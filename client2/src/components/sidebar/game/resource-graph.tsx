import React from 'react'

interface Props {
    active: boolean
}

export const ResourceGraph: React.FC<Props> = (props: Props) => {

    const columnClassName = "w-[50px] text-center mx-auto"

    const columns: Array<[string, React.ReactElement]> = [
        ["HQ", <p className={columnClassName}>HQ</p>],
        ["Robot 1", <p className={columnClassName}>R1</p>],
        ["Robot 2", <p className={columnClassName}>R2</p>],
    ];

    const data: Array<[string, Array<number>]> = [
        ["Count", [0, 0, 0]],
        ["Σ(HP)", [0, 0, 0]],
    ]

    return (
        <table className="my-2 w-full">
            <tr className="mb-4">
                <th className="pb-1"></th>
                {columns.map(column => 
                    <th className="pb-1" title={column[0]}>
                        {column[1]}
                    </th>)
                }
            </tr>
            {data.map(dataRow => <tr>
                <th className="text-sm">{dataRow[0]}</th>
                {dataRow[1].map(value => <td className="text-center text-sm">
                    {value}
                </td>)}
            </tr>)}
        </table>
    )
}
