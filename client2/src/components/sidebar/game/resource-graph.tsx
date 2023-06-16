import React from 'react'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface Props {
    active: boolean
}

const data = [
    {
        name: 'Page A',
        uv: 4000,
        pv: 2400,
        amt: 2400
    },
    {
        name: 'Page B',
        uv: 3000,
        pv: 1398,
        amt: 2210
    },
    {
        name: 'Page C',
        uv: 2000,
        pv: 9800,
        amt: 2290
    },
    {
        name: 'Page D',
        uv: 2780,
        pv: 3908,
        amt: 2000
    },
    {
        name: 'Page E',
        uv: 1890,
        pv: 4800,
        amt: 2181
    },
    {
        name: 'Page F',
        uv: 2390,
        pv: 3800,
        amt: 2500
    },
    {
        name: 'Page G',
        uv: 3490,
        pv: 4300,
        amt: 2100
    }
]

export const ResourceGraph: React.FC<Props> = (props: Props) => {
    const columnClassName = 'w-[50px] text-center mx-auto'

    const columns: Array<[string, React.ReactElement]> = [
        ['HQ', <p className={columnClassName}>HQ</p>],
        ['Robot 1', <p className={columnClassName}>R1</p>],
        ['Robot 2', <p className={columnClassName}>R2</p>]
    ]

    const data: Array<[string, Array<number>]> = [
        ['Count', [0, 0, 0]],
        ['Σ(HP)', [0, 0, 0]]
    ]

    return (
        <div className="my-2 w-full">
            <ResponsiveContainer
                aspect={1}
                width="100%"
            >
                <LineChart
                    data={data}
                    margin={{
                        top: 5,
                        right: 30,
                        left: 20,
                        bottom: 5
                    }}
                >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="pv" stroke="#8884d8" activeDot={{ r: 8 }} />
                    <Line type="monotone" dataKey="uv" stroke="#82ca9d" />
                </LineChart>
            </ResponsiveContainer>
        </div>
    )
}
