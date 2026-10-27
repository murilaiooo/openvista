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
<form id="entry" autocomplete="off" hidden><fieldset id="fields" disabled><label for="title">${text(R.string.pairing_title_label)}</label><input id="title" maxlength="120" autocomplete="off">
<label for="url">${text(R.string.pairing_url_label)}</label><input id="url" type="url" maxlength="4096" required autocomplete="off" spellcheck="false">
<label for="kind">${text(R.string.pairing_kind_label)}</label><select id="kind"><option value="HLS">HLS</option><option value="DASH">MPEG-DASH</option></select>
<button id="send" type="submit">${text(R.string.pairing_send)}</button></fieldset></form>
<p id="pending" role="status" hidden>${text(R.string.pairing_confirm_tv)}</p><p id="failure" role="alert" hidden>${text(R.string.pairing_phone_failed)}</p>
<script nonce="$nonce">'use strict';
(async()=>{let fragment=location.hash.slice(1);history.replaceState(null,'','/');
const form=document.getElementById('entry'),fields=document.getElementById('fields'),send=document.getElementById('send'),failure=document.getElementById('failure');
let key=null,active=true,controller=null;
const stop=()=>{active=false;key=null;fragment='';fields.disabled=true;form.reset();if(controller)controller.abort();};
addEventListener('pagehide',stop,{once:true});document.addEventListener('visibilitychange',()=>{if(document.hidden)stop();});setTimeout(stop,120000);
// HTTP cannot authenticate this page or provide WebCrypto. Never collect a private URL there.
if(location.protocol!=='https:'||!window.isSecureContext||!window.crypto||!crypto.subtle||!/^v1\.[A-Za-z0-9_-]{22}\.[A-Za-z0-9_-]{43}${'$'}/.test(fragment)){stop();failure.hidden=false;return;}
const decode=(s)=>Uint8Array.from(atob(s.replace(/-/g,'+').replace(/_/g,'/')),c=>c.charCodeAt(0));
const encode=(bytes)=>btoa(String.fromCharCode(...new Uint8Array(bytes))).replace(/\+/g,'-').replace(/\//g,'_').replace(/=+${'$'}/,'');
const parts=fragment.split('.'),sid=parts[1],raw=decode(parts[2]);fragment='';parts.fill('');
const aad=(direction)=>new TextEncoder().encode('OpenVista pairing v1\n'+sid+'\n'+location.host+'\n'+direction);
const responseText=async(result)=>{const reader=result.body.getReader();let size=0,text='';const decoder=new TextDecoder('utf-8',{fatal:true});try{while(true){const chunk=await reader.read();if(chunk.done)break;size+=chunk.value.byteLength;if(size>512)throw Error();text+=decoder.decode(chunk.value,{stream:true});}return text+decoder.decode();}finally{await reader.cancel();reader.releaseLock();}};
try{key=await crypto.subtle.importKey('raw',raw,'AES-GCM',false,['encrypt','decrypt']);raw.fill(0);if(!active){key=null;return;}fields.disabled=false;form.hidden=false;}catch(_){raw.fill(0);stop();failure.hidden=false;return;}
form.addEventListener('submit',async(event)=>{event.preventDefault();if(!active||!key)return;send.disabled=true;failure.hidden=true;
let timeout;
try{const iv=crypto.getRandomValues(new Uint8Array(12));const plaintext=new TextEncoder().encode(JSON.stringify({title:document.getElementById('title').value,url:document.getElementById('url').value,kind:document.getElementById('kind').value}));
if(plaintext.byteLength>6144){plaintext.fill(0);throw Error();}
const ciphertext=await crypto.subtle.encrypt({name:'AES-GCM',iv,additionalData:aad('source'),tagLength:128},key,plaintext);plaintext.fill(0);form.reset();
if(!active||!key)return;controller=new AbortController();timeout=setTimeout(()=>controller.abort(),5000);
const result=await fetch('/source',{method:'POST',cache:'no-store',credentials:'omit',redirect:'error',signal:controller.signal,headers:{'Content-Type':'application/json'},body:JSON.stringify({v:1,sid,iv:encode(iv),ciphertext:encode(ciphertext)})});
if(result.status!==202||Number(result.headers.get('Content-Length'))>512)throw Error();const ack=JSON.parse(await responseText(result));
if(!active||!key||ack.v!==1||ack.sid!==sid||!/^([A-Za-z0-9_-]{16})${'$'}/.test(ack.iv)||typeof ack.ciphertext!=='string'||ack.ciphertext.length>128)throw Error();
const accepted=await crypto.subtle.decrypt({name:'AES-GCM',iv:decode(ack.iv),additionalData:aad('ack'),tagLength:128},key,decode(ack.ciphertext));
if(!active||new TextDecoder('utf-8',{fatal:true}).decode(accepted)!=='pending-tv-approval')throw Error();stop();form.hidden=true;document.getElementById('pending').hidden=false;
}catch(_){stop();failure.hidden=false;}finally{clearTimeout(timeout);}});})();</script>
</body></html>"""
    }

    private fun escape(value: String): String = value.replace("&", "&amp;")
        .replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;")
}
