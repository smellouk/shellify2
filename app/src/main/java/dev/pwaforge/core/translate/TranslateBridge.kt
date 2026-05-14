package dev.pwaforge.core.translate

object TranslateBridge {

    fun buildScript(
        targetLang: String,
        instanceUrl: String,
        apiKey: String,
        autoTranslate: Boolean,
    ): String = """
(function() {
  if (window.__pwaforgeTranslateLoaded) return;
  window.__pwaforgeTranslateLoaded = true;

  const TARGET = '${targetLang.replace("'", "\\'")}';
  const INSTANCE = '${instanceUrl.trimEnd('/').replace("'", "\\'")}';
  const API_KEY = '${apiKey.replace("'", "\\'")}';

  console.log('[PWATranslate] instance=' + INSTANCE + ' target=' + TARGET + ' hasKey=' + !!API_KEY);

  async function translateBatch(texts) {
    console.log('[PWATranslate] POST /translate texts=' + texts.length);
    try {
      const res = await fetch(INSTANCE + '/translate', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(Object.assign({q: texts, source: 'auto', target: TARGET, format: 'text'}, API_KEY ? {api_key: API_KEY} : {})),
      });
      console.log('[PWATranslate] response status=' + res.status);
      const json = await res.json();
      if (json.error) { console.log('[PWATranslate] API error: ' + json.error); return texts; }
      return Array.isArray(json.translatedText) ? json.translatedText : [json.translatedText];
    } catch(e) {
      console.log('[PWATranslate] fetch error: ' + e);
      return texts;
    }
  }

  async function translatePage() {
    console.log('[PWATranslate] translatePage called');
    const walker = document.createTreeWalker(
      document.body,
      NodeFilter.SHOW_TEXT,
      { acceptNode: function(n) {
          const tag = n.parentElement && n.parentElement.tagName;
          if (['SCRIPT','STYLE','NOSCRIPT','CODE','PRE'].includes(tag)) return NodeFilter.FILTER_REJECT;
          return n.nodeValue.trim() ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_SKIP;
      }}
    );
    const nodes = [];
    while (walker.nextNode()) nodes.push(walker.currentNode);
    console.log('[PWATranslate] found ' + nodes.length + ' text nodes');

    for (let i = 0; i < nodes.length; i += 50) {
      const chunk = nodes.slice(i, i + 50);
      const translated = await translateBatch(chunk.map(function(n){ return n.nodeValue; }));
      chunk.forEach(function(n, j) { if (translated[j]) n.nodeValue = translated[j]; });
    }
    console.log('[PWATranslate] done');
  }

  window.__pwaforgeTranslate = translatePage;

  ${if (autoTranslate) "translatePage();" else ""}
})();
""".trimIndent()
}
