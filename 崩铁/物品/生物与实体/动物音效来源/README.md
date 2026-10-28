# 动物音效来源

动物叫声优先从 Freesound 检索。该站每条录音单独选择许可，使用前需查看对应音源页；不要仅凭网站名称推断授权。

本目录保存 2026-10-27 用户下载并用于 1.24.0 五种原创动物叫声的原始音频。原始文件保持未修改；游戏运行时音频另行转换，放在 `src/main/resources/assets/starrail_sim/sounds/animals/`。当前素材只替换环境叫声（ambient）；受伤和死亡声音没有对应录音，继续使用现有声音配置。

运行时文件保留原录音完整时长，不变调、不加混响；统一转为单声道、44.1 kHz、OGG Vorbis，并按各录音峰值归一到约 -6 dB，供游戏里试听再调。

| 生物 | 原始文件 | 来源与作者 | 许可 | 说明 |
|---|---|---|---|---|
| 晴穗角鹿 | `250191__hyperionn__elk-7.wav` | [Elk 7.wav](https://freesound.org/people/Hyperionn/sounds/250191/) — Hyperionn | CC BY 4.0 | 雄性麋鹿发情季鸣叫；发布时注明作者和来源。 |
| 星绒岩羊 | `840891__thekingofgeeks360__sheep-bleat-medium-perspective.wav` | [Sheep - Bleat; Medium Perspective](https://freesound.org/people/TheKingOfGeeks360/sounds/840891/) — TheKingOfGeeks360 | CC0 | 羊叫声。 |
| 林息觅食兽 | `473519__awr1001__oink.mp3` | [Oink.mp3](https://freesound.org/people/awr1001/sounds/473519/) — awr1001；原始猪叫 [Angry Pig Oinking](https://freesound.org/people/Jofae/sounds/352698/) — Jofae | CC0 | 猪的短促哼声；两个音源页均标为 CC0。 |
| 泽纹羽禽 | `668805__mbpl__chicken-clucking-1.wav` | [Chicken clucking 1](https://freesound.org/people/MBPL/sounds/668805/) — MBPL | CC0 | 轻柔的咯咯叫。 |
| 星砾驮兽 | `826753__salsero_classic__renill-de-cavall-horse-neigh.aiff` | [Renill de cavall / Horse Neigh](https://freesound.org/people/Salsero_classic/sounds/826753/) — Salsero_classic | CC0 | 马叫声。 |

Freesound 的下载需要登录。许可与使用规则以每条音源页及 [Freesound FAQ](https://freesound.org/help/faq/) 为准；若以后音源页许可或内容变化，应在再次分发前重新核对。
