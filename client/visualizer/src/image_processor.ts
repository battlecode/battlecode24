var sharp = require('sharp')
let bp = 'src/static/img/resources/'
let images = [
    'accelerating_anchor',
    'anchor',
    'adamantium',
    'adamantium_well',
    'adamantium_well_upgraded',
    'elixir',
    'elixir_well',
    'elixir_well_upgraded',
    'mana_well',
    'mana_well_upgraded',
    'mana'
]
for (let img_name of images)
    sharp(bp + img_name + '.png')
        .resize(64, 64)
        .toFile(bp + img_name + '_smaller.png', (err, info) => { console.log(err) })
bp = 'src/static/img/robots/'
images = [
    'blue_amplifier',
    'red_amplifier',
    'blue_booster',
    'blue_carrier',
    'blue_destabilizer',
    'blue_launcher',
    'blue_headquarters',
    'red_booster',
    'red_carrier',
    'red_destabilizer',
    'red_launcher',
    'red_headquarters',
]
for (let img_name of images)
    sharp(bp + img_name + '.png')
        .resize(64, 64)
        .toFile(bp + img_name + '_smaller.png', (err, info) => { console.log(err) })