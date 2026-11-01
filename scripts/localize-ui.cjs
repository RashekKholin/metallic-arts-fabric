// One-way source migration plus an idempotent catalog rebuild. English rendering is preserved.
const fs=require('node:fs'),path=require('node:path'),crypto=require('node:crypto');const root=path.resolve(__dirname,'..');
const enFile=path.join(root,'src/main/resources/assets/metallicarts/lang/en_us.json');const en=JSON.parse(fs.readFileSync(enFile,'utf8'));
function key(text){const id='ui.metallicarts.'+crypto.createHash('sha1').update(text).digest('hex').slice(0,12);en[id]=text;return id;}
function scan(expr,visit){let quote=false,escape=false,depth=0;for(let i=0;i<expr.length;i++){const c=expr[i];if(quote){if(escape)escape=false;else if(c==='\\')escape=true;else if(c==='"')quote=false;continue;}if(c==='"'){quote=true;continue;}if('([{'.includes(c))depth++;else if(')]}'.includes(c))depth--;else if(depth===0)visit(c,i);}}
function literal(expr){try{if(/^"(?:[^"\\]|\\.)*"$/.test(expr))return JSON.parse(expr);}catch{}return null;}
function component(expr){expr=expr.trim();let question=-1,colon=-1,ternary=0;scan(expr,(c,i)=>{if(c==='?'){if(question<0)question=i;ternary++;}else if(c===':'&&question>=0&&colon<0&&--ternary===0)colon=i;});if(question>=0&&colon>question)return '('+expr.slice(0,question)+'?'+component(expr.slice(question+1,colon))+':'+component(expr.slice(colon+1))+')';
 const direct=literal(expr);if(direct!==null)return 'net.minecraft.network.chat.Component.translatable('+JSON.stringify(key(direct))+')';
 let splits=[];scan(expr,(c,i)=>{if(c==='+')splits.push(i);});if(splits.length){let start=0,parts=[];for(const index of [...splits,expr.length]){parts.push(expr.slice(start,index).trim());start=index+1;}if(parts.some(p=>literal(p)!==null)&&parts.every(Boolean)){let template='',args=[];for(const p of parts){const text=literal(p);if(text!==null)template+=text.replace(/%/g,'%%');else{template+='%s';args.push('('+p+')');}}return 'net.minecraft.network.chat.Component.translatable('+JSON.stringify(key(template))+(args.length?', '+args.join(', '):'')+')';}}
 return 'Component.literal('+expr+')';
}
function migrate(file){let source=fs.readFileSync(file,'utf8'),out='',start=0,index=0;while((index=source.indexOf('Component.literal(',start))>=0){out+=source.slice(start,index);let i=index+'Component.literal('.length,begin=i,depth=1,quote=false,escape=false;for(;i<source.length;i++){const c=source[i];if(quote){if(escape)escape=false;else if(c==='\\')escape=true;else if(c==='"')quote=false;continue;}if(c==='"')quote=true;else if(c==='(')depth++;else if(c===')'&&--depth===0)break;}if(depth!==0)throw Error(file);out+=component(source.slice(begin,i));start=i+1;}out+=source.slice(start);if(out!==source)fs.writeFileSync(file,out);}
function walk(dir){return fs.readdirSync(dir,{withFileTypes:true}).flatMap(e=>e.isDirectory()?walk(path.join(dir,e.name)):[path.join(dir,e.name)]);}
for(const file of walk(path.join(root,'src/client/java/dev/metallicarts/client')).filter(f=>f.endsWith('.java')))migrate(file);
for(const name of ['MaterialItem.java','MetalVialItem.java'])migrate(path.join(root,'src/main/java/dev/metallicarts/economy',name));
for(const file of walk(path.join(root,'src/main/java')).filter(f=>f.endsWith('.java'))){const source=fs.readFileSync(file,'utf8');for(const match of source.matchAll(/(?:IllegalArgumentException|Component\.literal)\("((?:[^"\\]|\\.)*)"\)/g)){try{key(JSON.parse('"'+match[1]+'"'));}catch{}}}
fs.writeFileSync(enFile,JSON.stringify(en,null,2)+'\n');
const aliases={};for(const [k,v] of Object.entries(en))if(k.startsWith('ui.metallicarts.')&&!v.includes('%s'))aliases[v]=k;
fs.writeFileSync(path.join(root,'src/main/resources/metallicarts/text_aliases.json'),JSON.stringify(aliases,null,2)+'\n');
console.log(JSON.stringify({uiKeys:Object.keys(en).filter(k=>k.startsWith('ui.metallicarts.')).length,aliases:Object.keys(aliases).length}));
