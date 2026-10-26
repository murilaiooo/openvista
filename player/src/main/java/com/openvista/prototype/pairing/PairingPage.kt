package com.openvista.prototype.pairing

import android.content.Context
import com.openvista.prototype.R

/** A self-contained phone page; no third-party script, font, image, storage or URL probes. */
internal object PairingPage {
    fun render(context: Context, nonce: String): String {
        fun text(id: Int) = escape(context.getString(id))
        val language = if (context.resources.configuration.locales[0].language == "pt") "pt-BR" else "en"
        return """<!doctype html>
<html lang="$language"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>${text(R.string.pairing_page_title)}</title>
<style>body{font:18px system-ui;max-width:34rem;margin:2rem auto;padding:1rem;color:#eee;background:#17212b}label,input,select,button{display:block;box-sizing:border-box;width:100%;margin:.7rem 0;font:inherit}input,select,button{padding:.7rem}button{cursor:pointer}p{line-height:1.5}</style></head>
<body><h1>${text(R.string.pairing_page_title)}</h1><p>${text(R.string.pairing_http_warning)}</p>
<form id="entry" autocomplete="off"><label for="title">${text(R.string.pairing_title_label)}</label><input id="title" maxlength="120" autocomplete="off">
<label for="url">${text(R.string.pairing_url_label)}</label><input id="url" type="url" maxlength="4096" required autocomplete="off" spellcheck="false">
<label for="kind">${text(R.string.pairing_kind_label)}</label><select id="kind"><option value="HLS">HLS</option><option value="DASH">MPEG-DASH</option></select>
<button id="send" type="submit">${text(R.string.pairing_send)}</button></form>
<p id="pending" role="status" hidden>${text(R.string.pairing_confirm_tv)}</p><p id="failure" role="alert" hidden>${text(R.string.pairing_phone_failed)}</p>
<script nonce="$nonce">'use strict';
(()=>{let token=location.hash.slice(1);history.replaceState(null,'','/');
const form=document.getElementById('entry'),send=document.getElementById('send'),failure=document.getElementById('failure');
if(!/^[A-Za-z0-9_-]{43}${'$'}/.test(token)){send.disabled=true;failure.hidden=false;return;}
form.addEventListener('submit',async(event)=>{event.preventDefault();send.disabled=true;failure.hidden=true;
try{const result=await fetch('/source',{method:'POST',cache:'no-store',credentials:'omit',redirect:'error',headers:{'Content-Type':'application/json','Authorization':'Bearer '+token},body:JSON.stringify({title:document.getElementById('title').value,url:document.getElementById('url').value,kind:document.getElementById('kind').value})});
if(result.status!==202)throw Error();token='';document.getElementById('url').value='';form.hidden=true;document.getElementById('pending').hidden=false;
}catch(_){token='';failure.hidden=false;}});setTimeout(()=>{token='';send.disabled=true;},120000);})();</script>
</body></html>"""
    }

    private fun escape(value: String): String = value.replace("&", "&amp;")
        .replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;")
}
