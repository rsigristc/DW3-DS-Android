"""Extract factual companion data from the user's Harmony disc, without copying game code.

Requires the local Harmony development kit and its MIPS audit runtime. The real
disc bootstraps and profile/MP routines run in Unicorn; source manifests are not
assumed to describe the newer Terriermon v50 disc.
"""
import argparse
import hashlib
import json
import re
import struct
import sys
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--harmony-root', type=Path, required=True)
    parser.add_argument('--disc', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--public-beta', action='store_true')
    args = parser.parse_args()
    root = args.harmony_root
    sys.path[:0] = [str(root / 'tools/mods'), str(root / 'tools/tests')]
    import build_mod_patch as engine
    from mips_mod_runtime import Runtime

    image = engine.CdImage(args.disc)
    try:
        exe = image.read_file('SLES_039.36')
        original_exe = (root / 'dump/SLES_039.36').read_bytes()
        export_file = args.disc.with_name('export.json')
        export_manifest = json.loads(export_file.read_text())['files'] if export_file.exists() else {}

        def game_file(path):
            # Harmony's loader uses its EXE file table; ISO directory entries
            # can still point to superseded stock resources.
            old_lba, _ = image.file(path)
            indices = [i for i in range(2382) if struct.unpack_from('<I', original_exe, 0x3576C + 4 * i)[0] == old_lba]
            assert len(indices) == 1, path
            index = indices[0]
            lba = struct.unpack_from('<I', exe, 0x3576C + 4 * index)[0]
            sectors = struct.unpack_from('<H', exe, 0x37CA4 + 2 * index)[0]
            data = b''.join(image.block(lba + i) for i in range(sectors))
            if str(index) in export_manifest:
                row = export_manifest[str(index)]
                data = data[:row['bytes']]
                assert hashlib.sha256(data).hexdigest() == row['sha256'], path
            return data
        kit = json.loads((root / 'decomp/mods/item-kit/bundle.json').read_text())
        runtime = Runtime('build/experiments/ninth-partner-active/resident', binary='active.bin')
        runtime.write(0x80010000, exe[2048:])
        runtime.write(0x8005CCE8, bytes(0x80082CB0 - 0x8005CCE8))
        runtime.stub(lambda *a: 0, 0xA0)  # BIOS cache flush, not game logic.
        runtime.instruction_limit = 3_000_000
        runtime.call(kit['bootstrap_base'])
        codec = engine.stage_text.mapping(root)

        def names(filename):
            data = game_file(filename)
            return [engine.stage_text.decode(row, codec) for row in
                    engine.battle_ui_plus.text_entries(data, struct.unpack_from('<I', data)[0])]

        digimon_names = names('AAA/DAT/COUNTRY/ENG/ESDIGNAM.BIN')
        skill_en = names('AAA/DAT/COUNTRY/ENG/ESSKLNAM.BIN')
        skill_es = names('AAA/DAT/COUNTRY/SPN/SSSKLNAM.BIN')
        extra_names = {p['id']: p['name'] for p in json.loads(
            (root / 'decomp/mods/progress-ui-plus/agumon_profiles.json').read_text())}
        extra_names.update({0x6100: 'Terriermon', 0x6101: 'Gargomon', 0x6102: 'Rapidmon'})
        ids = [struct.unpack_from('<H', exe, 0x8003EF5C - 0x8000F800 + i * 88)[0] for i in range(55)]
        ids += list(range(0x6001, 0x603D))
        if not args.public_beta:
            ids += [0x6100, 0x6101, 0x6102]
        rule_getters = [0x800AD860, 0x800AD880, 0x800AD870, 0x800AD808,
                        0x800AD890, 0x800AD850, 0x800AD830, 0x800AD840]
        result = dict(schema=1, revision='Harmony Beta 1.5 (r48)' if args.public_beta else 'Harmony Terriermon v50', ninthPartner=not args.public_beta,
                      discSha256=hashlib.file_digest(args.disc.open('rb'), 'sha256').hexdigest(),
                      hookOffset=0x134E8, hook=runtime.read(0x800134E8, 8).hex(), profiles=[])
        terrier_pointer = runtime.call(0x800134E8, 0x6001 if args.public_beta else 0x6100)
        result['profileOffset'] = terrier_pointer & 0x1FFFFF
        result['profileFingerprint'] = runtime.read(terrier_pointer, 88).hex()
        for ident in ids:
            pointer = runtime.call(0x800134E8, ident)
            if pointer == 0:  # Native unused profiles 468..470 are not playable.
                assert ident in (468, 469, 470)
                continue
            raw = runtime.read(pointer, 88)
            assert struct.unpack_from('<H', raw)[0] == ident, (hex(ident), hex(pointer), raw[:4].hex())
            skills = struct.unpack_from('<7H', raw, 28)
            rules = {}
            for member, getter in enumerate(rule_getters):
                rule = runtime.call(getter, ident)
                if rule:
                    rid, count = struct.unpack('<hh', runtime.read(rule, 4))
                    assert rid == ident and 0 <= count <= 6
                    rules[str(member)] = [list(struct.unpack('<hhh', runtime.read(rule + 4 + i * 6, 6)))
                                          for i in range(count)]
            name = digimon_names[raw[85]] if raw[85] < len(digimon_names) else extra_names[ident]
            result['profiles'].append(dict(id=ident, name=name,
                                            skills=[], status=list(raw[44:49]), rules=rules,
                                            expRate=raw[62], expLevel=raw[60]))
            profile = result['profiles'][-1]
            for slot, skill in enumerate(skills[1:], 1):
                if not skill:
                    continue
                tech = runtime.read(0x80042BDC + (skill - 1) * 18, 18)
                profile['skills'].append(dict(id=skill, name=skill_en[skill], nameEs=skill_es[skill],
                                               level=raw[48 + slot], mastery=raw[54 + slot] if slot < 6 else None,
                                               mp=struct.unpack_from('<H', tech)[0],
                                               power=struct.unpack_from('<H', tech, 2)[0],
                                               signature=slot == 6))
        terrier_rules = {0x6101: [[40, 0, 5]], 0x6102: [[40, 0, 20], [41, 0x6101, 15]],
                         372: [[40, 0, 43], [41, 0x6102, 35], [40, 7, 300]],
                         0x602E: [[40, 0, 60], [41, 372, 45], [40, 7, 350]]}
        ram = runtime.read(0x80000000, 0x200000)
        for ident, conditions in ({} if args.public_beta else terrier_rules).items():
            encoded = struct.pack('<hh', ident, len(conditions)) + b''.join(struct.pack('<hhh', *c) for c in conditions)
            assert encoded in ram, 'Terriermon evolution rules do not match this disc'
            next(p for p in result['profiles'] if p['id'] == ident)['rules']['8'] = conditions

        # Run the exact MP calculation for the supported accessories and loaded
        # skill flag. Includes scoped r49/r50 overrides and form-tier discounts.
        runtime.write(0x80082CB0, game_file('AAA/PRO/FIGHTSTG.PRO'))
        fight_symbols = (root / 'decomp/reference-src/juandav-98e8043/config/eu/symbols_fightstg.txt').read_text()
        cost_address = int(re.search(r'^func_800A0DA4 = (0x[0-9A-Fa-f]+)', fight_symbols, re.M)[1], 16)
        runtime.word(0x800A4460 + 8, 0)
        runtime.word(0x80048DA4, 0)
        rings = (0, 0x143, 0x144)
        result['techniques'] = []
        all_skills = sorted({s['id'] for p in result['profiles'] for s in p['skills']})
        runtime.half(0x800A4460 + 16, 5)  # Greymon has no form-tier MP discount.
        for ident in all_skills:
            raw = runtime.read(0x80042BDC + (ident - 1) * 18, 18)
            costs = []
            for loaded in (False, True):
                for first in rings:
                    for second in rings:
                        runtime.half(0x8004949C + 0x3C0 + 8, first)
                        runtime.half(0x8004949C + 0x3C0 + 10, second)
                        costs.append(runtime.call(cost_address, 0, ident | (0x4000 if loaded else 0)))
            result['techniques'].append(dict(id=ident, name=skill_en[ident], nameEs=skill_es[ident],
                                              level=1, mastery=None, signature=False,
                                              power=struct.unpack_from('<H', raw, 2)[0], costs=costs))
        for profile in result['profiles']:
            runtime.half(0x800A4460 + 16, profile['id'])
            runtime.half(0x8004949C + 0x3C0 + 8, 0)
            runtime.half(0x8004949C + 0x3C0 + 10, 0)
            reference_cost = runtime.call(cost_address, 0, 137)  # Flame Ball: native 20 MP.
            assert reference_cost in (17, 18, 20)
            profile['costPercent'] = reference_cost * 5
            for skill in profile['skills']:
                costs = []
                for loaded in (False, True):
                    for first in rings:
                        for second in rings:
                            runtime.half(0x8004949C + 0x3C0 + 8, first)
                            runtime.half(0x8004949C + 0x3C0 + 10, second)
                            try:
                                costs.append(runtime.call(cost_address, 0, skill['id'] | (0x4000 if loaded else 0)))
                            except Exception as error:
                                from unicorn.mips_const import UC_MIPS_REG_PC
                                raise RuntimeError((profile['id'], skill['id'], loaded, first, second,
                                                    hex(runtime.uc.reg_read(UC_MIPS_REG_PC)))) from error
                skill['costs'] = costs
                if (profile['id'], skill['id']) == (150, 101):
                    skill['power'] = 800
                if (profile['id'], skill['id']) == (0x603C, 97):
                    skill['power'] = 1000
        if not args.public_beta:
            assert next(p for p in result['profiles'] if p['id'] == 0x601B)['skills'][-1]['costs'][0] == 162
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(result, ensure_ascii=False, separators=(',', ':')) + '\n', encoding='utf-8')
        print(f"Exported {len(result['profiles'])} profiles and {sum(len(p['skills']) for p in result['profiles'])} skill assignments")
    finally:
        image.close()


if __name__ == '__main__':
    main()
