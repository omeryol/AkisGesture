import os
import json
import xml.sax.saxutils as saxutils
import xml.etree.ElementTree as ET

def generate_strings_xml(lang_code, lang_folder, translations, plurals=None, arrays=None):
    with open('tools/i18n/en_strings.json', 'r', encoding='utf-8') as f:
        en_items = json.load(f)

    en_strings = [it for it in en_items if it['tag'] == 'string']
    en_keys = [it['name'] for it in en_strings]

    # Verify parity
    missing = set(en_keys) - set(translations.keys())
    extra = set(translations.keys()) - set(en_keys)
    if missing:
        raise ValueError(f"[{lang_code}] Missing {len(missing)} keys: {list(missing)[:10]}...")
    if extra:
        raise ValueError(f"[{lang_code}] Extra {len(extra)} keys: {list(extra)[:10]}...")

    out_dir = os.path.join('app/src/main/res', lang_folder)
    os.makedirs(out_dir, exist_ok=True)
    out_file = os.path.join(out_dir, 'strings.xml')

    lines = ['<?xml version="1.0" encoding="utf-8"?>', '<resources>']

    for item in en_items:
        tag = item['tag']
        name = item['name']
        if tag == 'string':
            val = translations[name]
            # Android XML escaping
            # Check if already escaped
            escaped = val.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
            escaped = escaped.replace('&amp;amp;', '&amp;').replace('&amp;lt;', '&lt;').replace('&amp;gt;', '&gt;')

            # Escape apostrophes unless in quotes
            # If string contains single quote and not starts/ends with double quotes:
            if "'" in escaped and not (escaped.startswith('"') and escaped.endswith('"')):
                # replace unescaped ' with \'
                # careful not to double escape \'
                parts = escaped.split(r"\'")
                escaped = r"\'".join(p.replace("'", r"\'") for p in parts)

            lines.append(f'  <string name="{name}">{escaped}</string>')
        elif tag == 'plurals' and plurals and name in plurals:
            lines.append(f'  <plurals name="{name}">')
            for q, qval in plurals[name].items():
                lines.append(f'    <item quantity="{q}">{qval}</item>')
            lines.append('  </plurals>')
        elif tag == 'string-array' and arrays and name in arrays:
            lines.append(f'  <string-array name="{name}">')
            for aitem in arrays[name]:
                a_escaped = aitem.replace('&', '&amp;').replace("'", r"\'")
                lines.append(f'    <item>{a_escaped}</item>')
            lines.append('  </string-array>')

    lines.append('</resources>')

    content = '\n'.join(lines) + '\n'
    with open(out_file, 'w', encoding='utf-8') as f:
        f.write(content)

    # Verify XML validity with ElementTree
    ET.parse(out_file)
    print(f"[{lang_code}] Successfully written and verified {out_file} ({len(translations)} strings)")

if __name__ == '__main__':
    print("generate_strings_xml helper ready.")
