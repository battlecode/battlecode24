const colors = require('tailwindcss/colors');

module.exports = {
    content: ['./src/**/*.{ts,tsx}'],
    theme: {
        fontFamily: {
            sans: ['Martian Mono', 'sans-serif']
        },
        extend: {
            transitionProperty: {
                'max-height': 'max-height'
            },
            boxShadow: {
                'centered': '0 0 10px 1px rgba(0,0,0,0.6)',
            },
            colors: {
                red: '#ff9194',
                pink: '#ffb4c1',
                green: '#00a28e',
                cyan: '#02a7b9',
                cyanDark: '#1899a7',
                blue: '#04a2d9',
                blueLight: '#26abd9',

                dark: colors.gray['800'],
                darkHighlight: colors.gray['900'],
                black: colors.black,
                light: '#faf9fe',
                lightHighlight: '#eeeeee',
                medHighlight: '#d0d0d0',

                team0: '#D53E43',
                team1: '#407496'
            }
        },
    },
    plugins: [],
};
