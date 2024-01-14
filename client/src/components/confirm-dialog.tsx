import React, { PropsWithChildren } from 'react'
import { BasicDialog } from './basic-dialog'

interface Props {
    open: boolean
    onConfirm: () => void
    onCancel: () => void
    title: string
    description?: string
    className?: string
}

export const ConfirmDialog: React.FC<PropsWithChildren<Props>> = (props) => {
    return (
        <BasicDialog {...props} onCancel={() => props.onCancel()} onConfirm={() => props.onConfirm()}>
            {props.children}
        </BasicDialog>
    )
}
