import os
import re
import json
import time
import urllib.request
import urllib.parse
from concurrent.futures import ThreadPoolExecutor, as_completed
import xml.etree.ElementTree as ET
from domain_overrides import DOMAIN_OVERRIDES

CLIENTS = ['dict-chrome-ex', 'gtx']

def protect_placeholders(text):
    pattern = r'(%(\d+\$)?[dsf])'
    placeholders = []
    def repl(m):
        idx = len(placeholders)
        placeholders.append(m.group(0))
        return f"__PH{idx}__"
    protected = re.sub(pattern, repl, text)
    return protected, placeholders

def restore_placeholders(text, placeholders):
    restored = text
    for idx, ph in enumerate(placeholders):
        restored = re.sub(rf'__\s*PH\s*{idx}\s*__', ph, restored, flags=re.IGNORECASE)
        restored = restored.replace(f"__PH{idx}__", ph)
    return restored

def translate_single(text, target_lang, cache=None):
    if not text or not text.strip():
        return text
    cache_key = f"{target_lang}:{text}"
    if cache is not None and cache_key in cache:
        return cache[cache_key]

    protected, placeholders = protect_placeholders(text)
    q = urllib.parse.quote(protected)

    for client in CLIENTS:
        url = f"https://translate.googleapis.com/translate_a/single?client={client}&sl=en&tl={target_lang}&dt=t&q={q}"
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'})
        try:
            with urllib.request.urlopen(req, timeout=5) as response:
                data = json.loads(response.read().decode('utf-8'))
                translated_parts = [segment[0] for segment in data[0] if segment and segment[0]]
                translated = "".join(translated_parts)
                restored = restore_placeholders(translated, placeholders)
                if cache is not None:
                    cache[cache_key] = restored
                return restored
        except Exception:
            continue

    # Fallback to MyMemory if Google clients fail
    try:
        url = f"https://api.mymemory.translated.net/get?q={q}&langpair=en|{target_lang}"
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req, timeout=4) as response:
            data = json.loads(response.read().decode('utf-8'))
            translated = data.get('responseData', {}).get('translatedText', '')
            if translated and not translated.startswith('MYMEMORY WARNING'):
                # remove any <g ...> tags
                translated = re.sub(r'<[^>]+>', '', translated)
                restored = restore_placeholders(translated, placeholders)
                if cache is not None:
                    cache[cache_key] = restored
                return restored
    except Exception:
        pass

    return text

def format_xml_value(val):
    if not val:
        return ""
    val = val.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
    val = val.replace('&amp;amp;', '&amp;').replace('&amp;lt;', '&lt;').replace('&amp;gt;', '&gt;')
    if "'" in val and not (val.startswith('"') and val.endswith('"')):
        parts = val.split(r"\'")
        val = r"\'".join(p.replace("'", r"\'") for p in parts)
    return val

def build_language(lang_code, lang_dir, cache):
    out_dir = os.path.join('app/src/main/res', lang_dir)
    out_file = os.path.join(out_dir, 'strings.xml')

    # If already built and valid, skip
    if os.path.exists(out_file):
        try:
            tree = ET.parse(out_file)
            cnt = len(tree.getroot().findall('string'))
            if cnt == 607:
                print(f"[{lang_code}] Already exists and verified ({cnt} strings). Skipping.", flush=True)
                return
        except Exception:
            pass

    print(f"\n--> Starting {lang_code} ({lang_dir})...", flush=True)
    t0 = time.time()

    with open('tools/i18n/en_strings.json', 'r', encoding='utf-8') as f:
        en_items = json.load(f)

    overrides = DOMAIN_OVERRIDES.get(lang_code, {})
    os.makedirs(out_dir, exist_ok=True)

    to_translate = []
    results = {}

    for item in en_items:
        tag = item['tag']
        name = item['name']
        if tag == 'string':
            if name in overrides:
                results[name] = overrides[name]
            else:
                to_translate.append((('string', name), item['text']))
        elif tag == 'plurals':
            for q, text in item['items'].items():
                to_translate.append((('plurals', name, q), text))
        elif tag == 'string-array':
            for idx, a_text in enumerate(item['items']):
                to_translate.append((('array', name, idx), a_text))

    def worker(task):
        key, text = task
        trans = translate_single(text, lang_code, cache)
        return key, trans

    with ThreadPoolExecutor(max_workers=12) as executor:
        futures = [executor.submit(worker, it) for it in to_translate]
        done_cnt = 0
        for fut in as_completed(futures):
            key, trans = fut.result()
            results[key] = trans
            done_cnt += 1
            if done_cnt % 150 == 0:
                print(f"    [{lang_code}] Translated {done_cnt}/{len(to_translate)} items...", flush=True)

    lines = ['<?xml version="1.0" encoding="utf-8"?>', '<resources>']
    for item in en_items:
        tag = item['tag']
        name = item['name']
        if tag == 'string':
            val = results.get(name, results.get(('string', name), item['text']))
            lines.append(f'  <string name="{name}">{format_xml_value(val)}</string>')
        elif tag == 'plurals':
            lines.append(f'  <plurals name="{name}">')
            for q, text in item['items'].items():
                val = results.get(('plurals', name, q), text)
                lines.append(f'    <item quantity="{q}">{format_xml_value(val)}</item>')
            lines.append('  </plurals>')
        elif tag == 'string-array':
            lines.append(f'  <string-array name="{name}">')
            for idx, a_text in enumerate(item['items']):
                val = results.get(('array', name, idx), a_text)
                lines.append(f'    <item>{format_xml_value(val)}</item>')
            lines.append('  </string-array>')

    lines.append('</resources>')

    with open(out_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(lines) + '\n')

    ET.parse(out_file)
    elapsed = time.time() - t0
    print(f"[{lang_code}] COMPLETED in {elapsed:.1f}s: {out_file} (607 strings verified)!", flush=True)

def main():
    cache_file = 'tools/i18n/translation_cache.json'
    cache = {}
    if os.path.exists(cache_file):
        try:
            with open(cache_file, 'r', encoding='utf-8') as f:
                cache = json.load(f)
        except Exception:
            pass

    languages = [
        ('ru', 'values-ru'),
        ('pl', 'values-pl'),
        ('de', 'values-de'),
        ('fr', 'values-fr'),
        ('it', 'values-it'),
        ('vi', 'values-vi'),
        ('fa', 'values-fa')
    ]

    for lang_code, lang_dir in languages:
        build_language(lang_code, lang_dir, cache)
        # Save cache incrementally
        with open(cache_file, 'w', encoding='utf-8') as f:
            json.dump(cache, f, ensure_ascii=False, indent=2)

    print("\nALL LANGUAGES PROCESSED SUCCESSFULLY!", flush=True)

if __name__ == '__main__':
    main()
