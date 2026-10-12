"""Export derived game textures; keep full-size source illustrations intact."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
DEST = ROOT / 'src/main/resources/assets/starrail_sim/textures/gui/relics'
SOURCES = {
    'musketeer': ('外圈/野穗伴行的快枪手', ['快枪手的野穗毡帽', '快枪手的粗革手套', '快枪手的猎风披肩', '快枪手的铆钉马靴']),
    'passerby': ('外圈/云无留迹的过客', ['过客的逢春木簪', '过客的游龙臂鞲', '过客的残绣风衣', '过客的冥途游履']),
    'knight': ('外圈/净庭教宗的圣骑士', ['圣骑的宽恕盔面', '圣骑的沉默誓环', '圣骑的肃穆胸甲', '圣骑的秩序铁靴']),
    'space_station': ('内圈/太空封印站', ['「黑塔」的空间站点', '「黑塔」的漫历轨迹']),
    'fleet': ('内圈/不老者的仙舟', ['罗浮仙舟的天外楼船', '罗浮仙舟的建木枝蔓']),
}

def export(source, destination):
    destination.parent.mkdir(parents=True, exist_ok=True)
    with Image.open(source) as image:
        image.convert('RGBA').resize((64, 64), Image.Resampling.NEAREST).save(destination)

if __name__ == '__main__':
    for key, (folder, names) in SOURCES.items():
        parts = ['sphere', 'rope'] if folder.startswith('内圈') else ['head', 'hands', 'body', 'feet']
        for part, name in zip(parts, names):
            export(ROOT / '崩铁/遗器' / folder / (name + '.png'), DEST / key / (part + '.png'))
    for key, name in [('lost_crystal', '遗失晶块'), ('relic_remains', '遗器残骸'), ('self_modeling_resin', '自塑尘脂'), ('variable_dice', '变量骰子')]:
        export(ROOT / '崩铁/物品' / (name + '.png'), DEST / 'materials' / (key + '.png'))
    print('Exported 20 derived 64×64 GUI textures. Full-size source images preserved.')
