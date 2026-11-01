// Original data/model definitions. Vanilla texture references use the player's installed Minecraft assets.
const fs=require('node:fs'),path=require('node:path');
const root=path.resolve(__dirname,'../src/main/resources');
const write=(name,value)=>{const target=path.join(root,name);fs.mkdirSync(path.dirname(target),{recursive:true});fs.writeFileSync(target,JSON.stringify(value,null,2)+'\n');};
const langPath=path.join(root,'assets/metallicarts/lang/en_us.json');const lang=JSON.parse(fs.readFileSync(langPath,'utf8'));
const metals=['iron','steel','tin','pewter','zinc','brass','copper','bronze','aluminum','duralumin','chromium','nicrosil','gold','electrum','cadmium','bendalloy'];
for(const [name,texture] of [['mixed_vial','potion'],['medallion','clock'],['lerasium_bead','nether_star'],...metals.map(m=>['lerasium_'+m+'_bead','nether_star'])]){
 write('assets/metallicarts/items/'+name+'.json',{model:{type:'minecraft:model',model:'metallicarts:item/'+name}});
 write('assets/metallicarts/models/item/'+name+'.json',{parent:'minecraft:item/generated',textures:{layer0:'minecraft:item/'+texture}});
 lang['item.metallicarts.'+name]=name==='mixed_vial'?'Mixed Metal Vial':name==='medallion'?'Ability Core Medallion':name==='lerasium_bead'?'Lerasium Bead':'Lerasium–'+name.split('_')[1]+' Bead';
}
Object.assign(lang,{
 'metallicarts.guide.title':'Guide','metallicarts.guide.previous':'Previous','metallicarts.guide.next':'Next','metallicarts.guide.close':'Close',
 'metallicarts.guide.welcome':'The Metallic Arts: press M, then Guide. /metallicarts-survival guide also works. Powers need fuel or stored attributes.',
 'metallicarts.guide.snapped':'You survived a snapping event. Check M for any awakened innate Allomancy. This event cannot be repeated to regain stolen power.',
 'metallicarts.guide.0':'Start: mine the new ores in newly generated Overworld chunks. Craft a metallurgy table and choose its tabs to refine, alloy and shape metal. The README lists exact recipes and controls. Existing chunks do not gain new ore.',
 'metallicarts.guide.1':'Discovery: each character gets a once-only latent Misting/Ferring allocation. Default chance is 50% for each art. Survive at least 6 damage with 6 health or less to awaken Allomancy. At XP level 5, run /metallicarts-survival discover to reveal Feruchemy. Some characters have neither. These are configurable gameplay rules.',
 'metallicarts.guide.2':'Allomancy: prepare pure metal flakes and brew filled vials. Drink, then press M and select a metal to burn or flare. Owning a power does not create fuel. Stop burning to conserve reserves. Keyboard and mouse buttons share server validation.',
 'metallicarts.guide.3':'Feruchemy: hold one metal ring, bracelet, bracer or earring. Press F, choose a contact slot and equip. Store an attribute while accepting its real penalty, then tap the same mind later. Identity protects charge. Blank Identity while storing for unkeyed minds; unkeyed alone does not grant a power.',
 'metallicarts.guide.4':'Equipment: with two filled vials in opposite hands, /metallicarts-survival mix combines ordinary fuel. With two free charged unsealed Nicrosil ability cores, /metallicarts-survival bind makes a medallion; add a third the same way. /metallicarts-survival medallion 0 equips its cores in adjacent slots. Tap each core in F; use separate attribute minds for the borrowed powers.',
 'metallicarts.guide.5':'Advanced arts: P opens compounding and construct controls. Turn Intent ON for an action that consumes material. Compounding needs both arts, a charged held source and a compatible contacted destination; it consumes the whole source. H manages spikes. Player extraction requires host permission and donor consent. Construct recipes require actual prepared spikes, not empty equipment.',
 'metallicarts.guide.6':'Safety and recovery: Esc closes menus; vanilla Controls rebinds mod keys. Bandages heal one trauma level while still; Gold tapping also pays to heal trauma. Back up the entire world before upgrades. Do not copy charged items between worlds: canonical charge lives in world data. Hosts can audit owned references. God-metal recipes are experimental and disabled by default. See the README for limits and future work.'
});
write('assets/metallicarts/lang/en_us.json',lang);
write('assets/metallicarts/lang/es_es.json',{
 'metallicarts.guide.title':'Guía','metallicarts.guide.previous':'Anterior','metallicarts.guide.next':'Siguiente','metallicarts.guide.close':'Cerrar',
 'metallicarts.guide.welcome':'Las Artes Metálicas: pulsa M y después Guía. Los poderes necesitan metal o atributos almacenados.',
 'metallicarts.guide.snapped':'Has sobrevivido a un evento de activación. Consulta M. No puedes repetirlo para recuperar poderes robados.'
});
const rulesPath=path.join(root,'metallicarts/rules.json');const rules=JSON.parse(fs.readFileSync(rulesPath,'utf8'));
// Reconcile earlier implementation-status notes when regenerating the current catalog.
for(const rule of rules){if(rule.id==='uncertain_advanced_rules')rule.statement='Unknown god metals, complete canonical anatomy and Excisor metaphysics remain deferred. Compounding, prepared player constructs and Lerasium have documented bounded implementations.';rule.implementation=rule.implementation.replace('Invested fuel is reserved until Compounding is designed.','Legacy invested fuel stays segregated; canonical physical sources use a separate Compounding transaction.').replace('Identity-free filling, unsealed access and Compounding are not implemented.','Identity-free filling, finite unsealed access and canonical-once Compounding are separately implemented.').replace('full constructs and exceptional nonlethal extraction remain deferred.','Prepared player constructs and default-off exceptional extraction have explicit dependencies; arbitrary NPC constructs remain deferred.');}
const additions=[
 ['advanced_compounding','GAMEPLAY_POLICY','A compatible charged physical source is consumed once into a compatible contacted destination at the configured multiplier. Copper restores existing memories once without duplicating facts.','MetalmindLedger spent tombstones and Compounding preflight preserve source, type and Identity.'],
 ['construct_anatomy','INTERPRETATION','Prepared canonical spikes and paired innate Blessings enable bounded Koloss, Inquisitor and Kandra body templates; preparation costs and template slots are gameplay adaptations.','ConstructLedger and ConstructRuntime; author discussion: https://www.brandonsanderson.com/blogs/blog/annotation-mistborn-3-chapter-thirty-nine'],
 ['survival_acquisition','GAMEPLAY_POLICY','Once-only latent allocation, injury awakening, XP discovery, powered hostile population and the optional mistwraith ritual are survival adaptations, not verified cosmere genetics or a canonical human-to-mistwraith recipe.','ProgressionState persists independently of grants so extraction/death cannot reroll powers.'],
 ['medallion_carriers','INTERPRETATION','Two or three unsealed typed ability cores retain independent charge and leases. The assembly command is a gameplay substitute for the incompletely published Excisor process.','Medallions carrier unfolds into actual contact slots; no universal unlimited-power item.'],
 ['lerasium_alloys','INTERPRETATION','Pure Lerasium grants standard Allomancy; alloys grant the associated standard-metal Allomancy. Strength 2, crouch-use, bounded non-stacking and the optional boss/material acquisition recipe are gameplay choices.','LerasiumItem; author interview index https://wob.coppermind.net/adv_search/?tags=lerasium and https://wob.coppermind.net/events/9-calamity-philadelphia-signing/#e7624. Direct event retrieval was blocked during this audit; do not treat exact quantities as canon.'],
 ['unresolved_god_metals','DEFERRED','Pure Atium, Era-one Atium/electrum, Malatium, Harmonium, Trellium and other shard metals/alloys need separate evidenced designs. No invented unknown effect is enabled.','Stable sixteen-metal contracts are unchanged; Lerasium is separate.']
];
for(const [id,status,statement,implementation] of additions){const rule={id,status,statement,sources:[],implementation};const index=rules.findIndex(r=>r.id===id);if(index<0)rules.push(rule);else rules[index]=rule;}
write('metallicarts/rules.json',rules);
console.log('Generated advanced item models, English guide, Spanish navigation and provenance.');
