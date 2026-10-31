// Deterministic original pixel art and data. Run with Node; no external packages required.
const fs=require('node:fs'),path=require('node:path'),zlib=require('node:zlib');
const root=path.resolve(__dirname,'../src/main/resources');
const write=(p,v)=>{const f=path.join(root,p);fs.mkdirSync(path.dirname(f),{recursive:true});fs.writeFileSync(f,typeof v==='string'||Buffer.isBuffer(v)?v:JSON.stringify(v,null,2)+'\n');};
const metals=['iron','steel','tin','pewter','zinc','brass','copper','bronze','aluminum','duralumin','chromium','nicrosil','gold','electrum','cadmium','bendalloy'];
const materials=[...metals,'silver','nickel','lead','bismuth','carbon','silicon'];
const colors=['aebac5','687783','d4dfdf','9fa5b0','96aab1','dbc058','d97b4c','b68945','e4edf3','bdd3df','b9d4db','a8bba0','efd656','e3dc9b','91a6cc','c8aacb','e1e7ed','afba92','7a8199','b998ca','454954','8e9ab1'];
const oreMap={tin:'tin',zinc:'zinc',silver:'silver',bauxite:'aluminum',nickel:'nickel',chromite:'chromium',lead:'lead',bismuth:'bismuth'};
const vanilla=new Set(['iron','copper','gold']);
const item=(m,k)=>vanilla.has(m)&&['ingot','nugget'].includes(k)?'minecraft:'+m+'_'+k:'metallicarts:'+m+'_'+k;
const human=s=>s.replaceAll('_',' ').replace(/\b\w/g,c=>c.toUpperCase());
let lang=JSON.parse(fs.readFileSync(path.join(root,'assets/metallicarts/lang/en_us.json'),'utf8'));
lang['container.metallicarts.metallurgy']='Metallurgy Table';
lang['screen.metallicarts.phase']='Phase 2 • metal economy';
lang['screen.metallicarts.legend']='A: Allomancy • F: Feruchemy • Esc: close';
function crc32(buf){let c=0xffffffff;for(const b of buf){c^=b;for(let j=0;j<8;j++)c=(c>>>1)^((c&1)?0xedb88320:0);}return(c^0xffffffff)>>>0;}
function png(p,pixel,w=16,h=16){
 const rows=Buffer.alloc(h*(w*4+1));for(let y=0;y<h;y++)for(let x=0;x<w;x++){const c=pixel(x,y);let i=y*(w*4+1)+1+x*4;for(let j=0;j<4;j++)rows[i+j]=c[j]??255;}
 const chunk=(t,d)=>{const b=Buffer.alloc(d.length+12);b.writeUInt32BE(d.length);b.write(t,4);d.copy(b,8);b.writeUInt32BE(crc32(Buffer.concat([Buffer.from(t),d])),8+d.length);return b;};
 const head=Buffer.alloc(13);head.writeUInt32BE(w);head.writeUInt32BE(h,4);head[8]=8;head[9]=6;
 write(p,Buffer.concat([Buffer.from([137,80,78,71,13,10,26,10]),chunk('IHDR',head),chunk('IDAT',zlib.deflateSync(rows)),chunk('IEND',Buffer.alloc(0))]));
}
function texture(name,kind,color){
 const rgb=color.match(/../g).map(n=>parseInt(n,16));png('assets/metallicarts/textures/item/'+name+'.png',(x,y)=>{
 let on=false;
 if(kind==='ingot')on=y>=5&&y<=11&&x>=3+(y<7?2:0)&&x<=12;
 else if(kind==='nugget'||kind==='portion')on=(x-8)**2+(y-8)**2<(kind==='portion'?12:24);
 else if(kind==='flakes')on=[[4,5],[9,4],[7,9],[12,10],[3,12]].some(([a,b])=>Math.abs(x-a)<=1&&Math.abs(y-b)<=1);
 else if(kind==='coin')on=((x-8)/1.2)**2+(y-8)**2<30;
 else if(['ring','bracelet','bracer'].includes(kind)) {const r=(x-8)**2+(y-8)**2;on=r<40&&r>(kind==='bracer'?5:kind==='bracelet'?10:15);}
 else if(kind==='spike')on=(y>2&&y<13&&Math.abs(x-8)<(y<5?3:1.6))||(y===13&&x===8);
 else if(kind==='vial') {if(y>=5&&y<=13&&x>=5&&x<=11){if(x===5||x===11||y===13)return[185,219,233,230];on=y>=9;}if(y>=2&&y<=4&&x>=6&&x<=10)return[118,83,52,255];}
 else on=(x-8)**2+(y-8)**2<35&&((x*7+y*3)%11<9);
 if(!on)return[0,0,0,0];let shade=((x*17+y*11)%19)-9+(y<7?22:0);return rgb.map(v=>Math.max(0,Math.min(255,v+shade))).concat(255);
 });}
function itemAsset(name,kind,color){lang['item.metallicarts.'+name]=human(name);write('assets/metallicarts/items/'+name+'.json',{model:{type:'minecraft:model',model:'metallicarts:item/'+name}});write('assets/metallicarts/models/item/'+name+'.json',{parent:'minecraft:item/generated',textures:{layer0:'metallicarts:item/'+name}});texture(name,kind,color);}
for(let n=0;n<materials.length;n++){const m=materials[n],color=colors[n];
 for(const k of ['ingot','nugget','portion','flakes'])if(!vanilla.has(m)||!['ingot','nugget'].includes(k))itemAsset(m+'_'+k,k,color);
 if(metals.includes(m))for(const k of ['coin','ring','bracelet','bracer','spike','vial'])itemAsset(m+'_'+k,k,color);
 write('data/c/tags/item/ingots/'+m+'.json',{replace:false,values:[item(m,'ingot')]});
 write('data/c/tags/item/nuggets/'+m+'.json',{replace:false,values:[item(m,'nugget')]});
 lang['tag.item.c.ingots.'+m]=human(m)+' Ingots';
 lang['tag.item.c.nuggets.'+m]=human(m)+' Nuggets';
}
itemAsset('invalid_alloy','raw','695c6b');lang['item.metallicarts.invalid_alloy']='Invalid Alloy Residue';
function blockAssets(name,model){lang['block.metallicarts.'+name]=human(name);write('assets/metallicarts/blockstates/'+name+'.json',{variants:{'':{model:'metallicarts:block/'+name}}});write('assets/metallicarts/models/block/'+name+'.json',model);write('assets/metallicarts/items/'+name+'.json',{model:{type:'minecraft:model',model:'metallicarts:block/'+name}});}
const ironLoot={type:'minecraft:block',pools:[{rolls:1,entries:[{type:'minecraft:alternatives',children:[
 {type:'minecraft:item',name:'minecraft:iron_ore',conditions:[{condition:'minecraft:match_tool',predicate:{predicates:{'minecraft:enchantments':[{enchantments:'minecraft:silk_touch',levels:{min:1}}]}}}]},
 {type:'minecraft:item',name:'minecraft:raw_iron',functions:[{function:'minecraft:apply_bonus',enchantment:'minecraft:fortune',formula:'minecraft:ore_drops'},{function:'minecraft:explosion_decay'}]}
]}]}],random_sequence:'minecraft:blocks/iron_ore'};
for(const [ore,m]of Object.entries(oreMap)){
 itemAsset('raw_'+ore,'raw',colors[materials.indexOf(m)]);
 itemAsset(ore+'_concentrate','raw',colors[materials.indexOf(m)]);
 write('data/c/tags/item/raw_materials/'+m+'.json',{replace:false,values:['metallicarts:raw_'+ore]});
 lang['tag.item.c.raw_materials.'+m]='Raw '+human(m);
 for(const deep of [false,true]){
  const name=(deep?'deepslate_':'')+ore+'_ore',rgb=colors[materials.indexOf(m)].match(/../g).map(n=>parseInt(n,16));
  png('assets/metallicarts/textures/block/'+name+'.png',(x,y)=>{const speck=((x*7+y*13+x*y)%23)<5;let base=(deep?58:118)+((x*13+y*7)%23)-11;return speck?rgb.map(v=>Math.min(255,v+((x+y)%3)*8)).concat(255):[base,base+2,base+3,255];});
  blockAssets(name,{parent:'minecraft:block/cube_all',textures:{all:'metallicarts:block/'+name}});
  let loot=JSON.stringify(ironLoot).replaceAll('minecraft:iron_ore','metallicarts:'+name).replaceAll('minecraft:raw_iron','metallicarts:raw_'+ore).replaceAll('minecraft:blocks/iron_ore','metallicarts:blocks/'+name);
  write('data/metallicarts/loot_table/blocks/'+name+'.json',JSON.parse(loot));
 }
 const size=['bauxite','tin','zinc'].includes(ore)?8:6;
 write('data/metallicarts/worldgen/configured_feature/'+ore+'_ore.json',{type:'minecraft:ore',config:{size,discard_chance_on_air_exposure:0,targets:[
  {target:{predicate_type:'minecraft:tag_match',tag:'minecraft:stone_ore_replaceables'},state:{Name:'metallicarts:'+ore+'_ore'}},
  {target:{predicate_type:'minecraft:tag_match',tag:'minecraft:deepslate_ore_replaceables'},state:{Name:'metallicarts:deepslate_'+ore+'_ore'}}]}});
 write('data/metallicarts/worldgen/placed_feature/'+ore+'_ore.json',{feature:'metallicarts:'+ore+'_ore',placement:[
  {type:'minecraft:count',count:['chromite','bismuth'].includes(ore)?4:8},{type:'minecraft:in_square'},
  {type:'minecraft:height_range',height:{type:'minecraft:uniform',min_inclusive:{absolute:ore==='bauxite'?0:-64},max_inclusive:{absolute:ore==='bauxite'?112:64}}},
  {type:'minecraft:biome'}]});
}
const oreNames=Object.keys(oreMap).flatMap(o=>[o+'_ore','deepslate_'+o+'_ore']);
write('data/minecraft/tags/block/mineable/pickaxe.json',{replace:false,values:[...oreNames,'metallurgy_table'].map(n=>'metallicarts:'+n)});
write('data/minecraft/tags/block/needs_stone_tool.json',{replace:false,values:[...oreNames,'metallurgy_table'].map(n=>'metallicarts:'+n)});
png('assets/metallicarts/textures/block/metallurgy_top.png',(x,y)=>{let edge=x<2||x>13||y<2||y>13;let grid=x===5||x===10||y===5||y===10;return edge?[91,57,35,255]:grid?[67,75,81,255]:[146+(x+y)%8,154+(x+y)%8,155,255];});
blockAssets('metallurgy_table',{parent:'minecraft:block/cube',textures:{particle:'metallicarts:block/metallurgy_top',up:'metallicarts:block/metallurgy_top',down:'minecraft:block/spruce_planks',north:'minecraft:block/smithing_table_front',south:'minecraft:block/smithing_table_front',east:'minecraft:block/smithing_table_side',west:'minecraft:block/smithing_table_side'}});
write('assets/metallicarts/blockstates/metallurgy_table.json',{variants:Object.fromEntries([0,1,2,3].map(i=>['specialty='+i,{model:'metallicarts:block/metallurgy_table'}]))});
write('data/metallicarts/loot_table/blocks/metallurgy_table.json',{type:'minecraft:block',pools:[{rolls:1,entries:[{type:'minecraft:item',name:'metallicarts:metallurgy_table'}],conditions:[{condition:'minecraft:survives_explosion'}]}]});
write('data/metallicarts/recipe/metallurgy_table.json',{type:'minecraft:crafting_shaped',category:'misc',pattern:['III','CCC','PPP'],key:{I:'minecraft:iron_ingot',C:'minecraft:cobblestone',P:'#minecraft:planks'},result:{id:'metallicarts:metallurgy_table',count:1}});
const recipes=[];
function recipe(id,tab,inputs,outputs,ticks=80){recipes.push({id,tab,ticks,inputs:inputs.map(([it,count])=>{
 const form=it.match(/^(?:minecraft|metallicarts):(.+)_(ingot|nugget)$/);
 if(form)return{tag:'c:'+form[2]+'s/'+form[1],count};
 const ore=it.match(/^(?:minecraft|metallicarts):raw_(.+)$/);
 if(ore)return{tag:'c:raw_materials/'+(oreMap[ore[1]]||ore[1]),count};
 return{item:it,count};
}),outputs:outputs.map(([it,count])=>({item:it,count}))});}
for(const [ore,m]of Object.entries(oreMap)){
 const outputs=ore==='zinc'?[[item('zinc','nugget'),8],[item('cadmium','nugget'),1]]:[[item(m,'ingot'),1]];
 recipe('concentrate_'+ore,0,[['metallicarts:raw_'+ore,1]],[['metallicarts:'+ore+'_concentrate',1]],80);
 recipe('refine_'+ore,0,[['metallicarts:'+ore+'_concentrate',1]],outputs,160);
}
for(const m of ['iron','copper','gold']){
 write('data/c/tags/item/raw_materials/'+m+'.json',{replace:false,values:['minecraft:raw_'+m]});
 lang['tag.item.c.raw_materials.'+m]='Raw '+human(m);
 itemAsset(m+'_concentrate','raw',colors[materials.indexOf(m)]);
 recipe('concentrate_'+m,0,[['minecraft:raw_'+m,1]],[['metallicarts:'+m+'_concentrate',1]],80);
 recipe('refine_'+m,0,[['metallicarts:'+m+'_concentrate',1]],[[item(m,'ingot'),1]],160);
}
recipe('carbon',0,[['minecraft:coal',1]],[[item('carbon','ingot'),1]],100);
recipe('charcoal_carbon',0,[['minecraft:charcoal',1]],[[item('carbon','ingot'),1]],100);
recipe('silicon',0,[['minecraft:sand',1]],[[item('silicon','ingot'),1]],160);
const ratios={steel:{iron:98,carbon:2},pewter:{tin:91,lead:9},brass:{copper:65,zinc:35},bronze:{copper:88,tin:12},electrum:{gold:45,silver:55},duralumin:{aluminum:96,copper:4},nicrosil:{nickel:84,chromium:14,silicon:2},bendalloy:{bismuth:50,lead:27,tin:13,cadmium:10}};
for(const [alloy,ratio]of Object.entries(ratios))recipes.push({id:'alloy_'+alloy,tab:1,ticks:200,inputs:Object.keys(ratio).map(m=>({item:item(m,'portion'),count:1})),outputs:[],alloy,ratio});
for(const m of materials){
 recipe(m+'_ingot_to_nuggets',2,[[item(m,'ingot'),1]],[[item(m,'nugget'),9]],40);
 recipe(m+'_nuggets_to_ingot',2,[[item(m,'nugget'),9]],[[item(m,'ingot'),1]],40);
 recipe(m+'_nugget_to_portions',2,[[item(m,'nugget'),1]],[[item(m,'portion'),100]],40);
 recipe(m+'_portions_to_nugget',2,[[item(m,'portion'),100]],[[item(m,'nugget'),1]],40);
 recipe(m+'_portion_to_flakes',2,[[item(m,'portion'),1]],[[item(m,'flakes'),1]],20);
 recipe(m+'_flakes_to_portion',2,[[item(m,'flakes'),1]],[[item(m,'portion'),1]],20);
 if(metals.includes(m)){
  recipe(m+'_coins',2,[[item(m,'nugget'),1]],[[item(m,'coin'),10]],40);
  recipe(m+'_coins_to_nugget',2,[[item(m,'coin'),10]],[[item(m,'nugget'),1]],40);
  for(const k of ['ring','bracelet','bracer','spike']){
   recipe(m+'_'+k,2,[[item(m,'ingot'),1]],[[item(m,k),1]],60);
   recipe(m+'_'+k+'_to_ingot',2,[[item(m,k),1]],[[item(m,'ingot'),1]],60);
  }
  for(const k of ['ingot','nugget','coin','ring','bracelet','bracer','spike'])recipes.push({id:m+'_'+k+'_invested_flaking',tab:2,ticks:40,inputs:[{item:item(m,k),count:1}],outputs:[{item:item(m,'flakes'),count:1}],slice:true});
 }
}
write('data/metallicarts/metallurgy/catalog.json',recipes);
// Deterministic access: every trade in a level's set is offered; no profession reroll lottery.
const specialties={
 metallurgist:['iron','tin','copper','zinc','lead','carbon'],
 alloy_metallurgist:Object.keys(ratios),
 rare_metallurgist:['aluminum','chromium','cadmium','gold','silver','nickel','bismuth','silicon'],
 equipment_metallurgist:metals
};
for(const tag of ['acquirable_job_site','village'])write('data/minecraft/tags/point_of_interest_type/'+tag+'.json',{replace:false,values:Object.keys(specialties).map(name=>'metallicarts:'+name)});
for(const [profession,list]of Object.entries(specialties)){
 lang['entity.minecraft.villager.metallicarts.'+profession]=human(profession);
 const color=['b88647','a6b5c3','bd9cca','70aa9b'][Object.keys(specialties).indexOf(profession)];
 // Original apron layer; profession overlay is the same for normal/zombie villagers.
 for(const family of ['villager','zombie_villager'])png('assets/metallicarts/textures/entity/'+family+'/profession/'+profession+'.png',(x,y)=>{
  const c=color.match(/../g).map(v=>parseInt(v,16));return x>=20&&x<28&&y>=20&&y<32?c.concat(230):[0,0,0,0];
 },64,64);
 for(let level=1;level<=5;level++){
  const ids=[];const add=(name,wants,gives,count,uses=12)=>{const id=profession+'/level_'+level+'/'+name;ids.push('metallicarts:'+id);write('data/metallicarts/villager_trade/'+id+'.json',{wants:{id:wants[0],count:wants[1]},gives:{id:gives,count},max_uses:uses,xp:level*5,reputation_discount:0.05});};
  if(profession==='equipment_metallurgist'){
   const k=level===1?'flakes':level===2?'coin':level===3?'ring':level===4?'spike':'flakes';
   for(const m of list)add(m,['minecraft:emerald',level===1?1:3],item(m,k),level===5?64:level===1?8:1);
   add('bottles',['minecraft:emerald',1],'minecraft:glass_bottle',8);
  }else if(level===1||level===2){
   // Level 1 offers the first half; level 2 adds the rest. Each set includes a buyback with a large spread.
   const half=Math.ceil(list.length/2),subset=level===1?list.slice(0,half):list.slice(half);
   for(const m of subset){
    add(m+'_sell',['minecraft:emerald',profession==='metallurgist'?1:3],item(m,'ingot'),1);
    add(m+'_buy',[item(m,'ingot'),8],'minecraft:emerald',1);
   }
  }else if(level===3){
   for(const m of list)add(m+'_portion',['minecraft:emerald',1],item(m,'portion'),32);
  }else if(level===4){
   for(const m of list)add(m+'_nuggets',['minecraft:emerald',2],item(m,'nugget'),4);
  }else{
   for(const m of list)add(m+'_bulk',['minecraft:emerald',profession==='metallurgist'?12:32],item(m,'ingot'),8,4);
  }
  write('data/metallicarts/trade_set/'+profession+'/level_'+level+'.json',{trades:ids,amount:ids.length,allow_duplicates:false});
 }
}
write('assets/metallicarts/lang/en_us.json',lang);
const rulesPath=path.join(root,'metallicarts/rules.json');
let rules=JSON.parse(fs.readFileSync(rulesPath,'utf8'));
const economyRules=[
 {id:'alloy_ratio_policy',status:'INTERPRETATION',statement:'The eight alloy ratios are documented gameplay interpretations. Exact and off-ratio batches are distinguished; these numbers are not claimed to be verified universal canon.',sources:[],implementation:'Data-driven alloy recipes with composition and viable/imperfect/invalid outcomes.'},
 {id:'material_accounting_policy',status:'GAMEPLAY_POLICY',statement:'One ingot equals nine nuggets, nine hundred measured portions, or nine grams of credited metal. Ore recovery yields and silicon/carbon processing are game abstractions, not real smelting chemistry.',sources:[],implementation:'Integer mass bookkeeping and recipe-conservation tests. Zinc refining credits eight zinc nuggets plus one cadmium nugget.'},
 {id:'unsafe_ingestion_policy',status:'INTERPRETATION',statement:'Non-Allomantic and off-ratio metals are refused by the brewing/drinking path rather than inventing canonical injury values. Drinking transfers fuel without granting access or activating a power.',sources:[],implementation:'Server-side vial validation. Invested slicing and ingestion preserve residual mass, charge and Identity.'}
];
rules=rules.filter(r=>!economyRules.some(e=>e.id===r.id)).concat(economyRules);
rules.find(r=>r.id==='identity_and_charged_items').implementation='Identity/charge persist through invested slicing, brewing and ingestion. Access checks, storage and tapping mechanics are deferred.';
rules.find(r=>r.id==='uncertain_advanced_rules').statement='God metals, anatomical binding points, Compounding, Fortune, Connection and unsealed metalmind behavior remain unresolved. Current alloy ratios are explicit gameplay interpretations rather than verified universal canonical quantities.';
rules.find(r=>r.id==='uncertain_advanced_rules').implementation='Advanced powers remain unimplemented. Eight interpreted alloy formulas are implemented in Phase 2.';
write('metallicarts/rules.json',rules);
// Version the small original textures as one deterministic archive. Gradle expands it with Java only.
function packTextures(){
 const files=[];const dir=path.join(root,'assets/metallicarts/textures');
 const walk=d=>{for(const name of fs.readdirSync(d).sort()){const full=path.join(d,name);if(fs.statSync(full).isDirectory())walk(full);else if(name.endsWith('.png'))files.push(full);}};walk(dir);
 const local=[],central=[];let offset=0;
 for(const file of files){
  const name=Buffer.from(path.relative(root,file).replaceAll(path.sep,'/')),data=fs.readFileSync(file),compressed=zlib.deflateRawSync(data),crc=crc32(data);
  const head=Buffer.alloc(30);head.writeUInt32LE(0x04034b50);head.writeUInt16LE(20,4);head.writeUInt16LE(8,8);head.writeUInt16LE(33,12);head.writeUInt32LE(crc,14);head.writeUInt32LE(compressed.length,18);head.writeUInt32LE(data.length,22);head.writeUInt16LE(name.length,26);
  local.push(head,name,compressed);
  const record=Buffer.alloc(46);record.writeUInt32LE(0x02014b50);record.writeUInt16LE(0x0314,4);record.writeUInt16LE(20,6);record.writeUInt16LE(8,10);record.writeUInt16LE(33,14);record.writeUInt32LE(crc,16);record.writeUInt32LE(compressed.length,20);record.writeUInt32LE(data.length,24);record.writeUInt16LE(name.length,28);record.writeUInt32LE((0o100644*65536)>>>0,38);record.writeUInt32LE(offset,42);
  central.push(record,name);offset+=head.length+name.length+compressed.length;
 }
 const directory=Buffer.concat(central),end=Buffer.alloc(22);end.writeUInt32LE(0x06054b50);end.writeUInt16LE(files.length,8);end.writeUInt16LE(files.length,10);end.writeUInt32LE(directory.length,12);end.writeUInt32LE(offset,16);
 fs.writeFileSync(path.resolve(__dirname,'../src/main/texture-assets.zip'),Buffer.concat([...local,directory,end]));
}
packTextures();
console.log(JSON.stringify({materials:materials.length,ores:oreNames.length,metallurgyRecipes:recipes.length,professions:4,levelsPerProfession:5}));
