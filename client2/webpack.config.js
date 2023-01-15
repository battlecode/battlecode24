var webpack = require('webpack')
const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')

module.exports = (env) => {
    const development = env.dev

    var config = {
        entry: './src/app.tsx',
        target: 'web',
        devtool: development ? 'source-map' : undefined,
        resolve: {
            extensions: ['.tsx', '.ts', '.js'],
        }, 
        module: {
            rules: [
                {
                    test: /\.ts(x?)$/,
                    include: /src/,
                    loader: 'ts-loader'
                },
                {
                    test: /\.css$/,
                    use: ['style-loader', 'css-loader', 'postcss-loader']
                },
                {
                    test: /\.(png|jpg|jpeg|gif|svg|ttf|otf|woff|woff2|eot)$/,
                    loader: 'url-loader'
                }
            ]
        },
        devServer: {
            compress: true,
            hot: true,
            port: 3000,
            static: {
              directory: path.join(__dirname, '/'),
            }
        },
        output: {
            path: path.resolve(__dirname, './dist'),
            filename: 'bundle.js'
        },
        plugins: [
            new HtmlWebpackPlugin({
                template: './index.html'
            }),
            new webpack.LoaderOptionsPlugin({
                minimize: !development,
                debug: development
            })
        ],
    }

    return config
}