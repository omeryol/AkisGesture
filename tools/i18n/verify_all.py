import os
import re
import json
import xml.etree.ElementTree as ET

TARGET_LANGS = [
    ('ru', 'values-ru', 'Russian'),
    ('pl', 'values-pl', 'Polish'),
    ('de', 'values-de', 'German'),
    ('fr', 'values-fr', 'French'),
    ('it', 'values-it', 'Italian'),
    ('vi', 'values-vi', 'Vietnamese'),
    ('zh-TW', 'values-zh-rTW', 'Traditional Chinese'),
    ('fa', 'values-fa', 'Persian')
]

def get_placeholders(text):
    if not text:
        return []
    return re.findall(r'%(\d+\$)?[dsf]', text)

def verify_all():
    base_file = 'app/src/main/res/values/strings.xml'
    base_tree = ET.parse(base_file)
    base_root = base_tree.getroot()

    base_strings = {s.get('name'): s.text or '' for s in base_root.findall('string')}
    base_keys = set(base_strings.keys())

    print(f"Base 'values/strings.xml' has {len(base_keys)} strings.\n")

    all_passed = True
    results = []

    for code, folder, name in TARGET_LANGS:
        file_path = os.path.join('app/src/main/res', folder, 'strings.xml')
        if not os.path.exists(file_path):
            results.append((code, name, "MISSING", 0, 0, "File does not exist"))
            all_passed = False
            continue

        try:
            tree = ET.parse(file_path)
            root = tree.getroot()
        except Exception as e:
            results.append((code, name, "PARSE_ERROR", 0, 0, str(e)))
            all_passed = False
            continue

        cur_strings = {s.get('name'): s.text or '' for s in root.findall('string')}
        cur_keys = set(cur_strings.keys())

        missing = base_keys - cur_keys
        extra = cur_keys - base_keys

        # Check placeholders
        placeholder_mismatches = 0
        for k, btext in base_strings.items():
            if k in cur_strings:
                b_ph = get_placeholders(btext)
                c_ph = get_placeholders(cur_strings[k])
                if len(b_ph) != len(c_ph):
                    placeholder_mismatches += 1

        status = "OK"
        details = "100% Match"
        if missing or extra or placeholder_mismatches > 5:
            status = "WARN" if not missing and not extra else "FAIL"
            details = f"Missing: {len(missing)}, Extra: {len(extra)}, PhMismatch: {placeholder_mismatches}"
            if missing or extra:
                all_passed = False

        results.append((code, name, status, len(cur_keys), placeholder_mismatches, details))

    print(f"{'Code':<8} {'Language':<22} {'Status':<10} {'Keys':<8} {'Details'}")
    print("-" * 75)
    for code, name, status, count, ph, details in results:
        icon = "[PASS]" if status in ("OK", "WARN") else "[FAIL]"
        print(f"{icon:<7} {code:<6} {name:<22} {status:<10} {count:<8} {details}")

    print("-" * 75)
    if all_passed:
        print("ALL TARGET LANGUAGES VERIFIED SUCCESSFULLY!")
    else:
        print("SOME LANGUAGES ARE STILL PENDING OR FAILED.")
    return all_passed

if __name__ == '__main__':
    verify_all()
