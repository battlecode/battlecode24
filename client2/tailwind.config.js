const colors = require('tailwindcss/colors')

module.exports = {
    content: ['./src/**/*.{ts,tsx}'],
    theme: {
        fontFamily: {
            sans: ['Martian Mono', 'sans-serif']
        },
        extend: {
            boxShadow: {
                'centered': '0 0 10px 1px rgba(0,0,0,0.6)',
            },
            colors: {
                bg: colors.gray['900'],
                bgHover: colors.gray['800'],
                fg: colors.pink['900'],
                buttonNeutral: colors.pink['600'],
                teamRed: '#b54543',
                teamRedHover: '#db5351',
                teamBlue: '#567495',
                teamBlueHover: '#6c91bb'
            }
        },
    },
    plugins: [],
}