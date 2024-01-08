import React from 'react'

type Props = {
    className?: string
}

export const ChevronUpIcon = (props: Props) => (
    <svg
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        strokeWidth={1.5}
        stroke="currentColor"
        className={(props.className ? props.className : "") + " w-6 h-6"}
    >
        <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 15.75l7.5-7.5 7.5 7.5" />
    </svg>
)
export const DoubleChevronUpIcon = (props: Props) => (
    <svg
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 40"
        strokeWidth={4}
        stroke="#ffd43b"
        className={(props.className ? props.className : "") + " w-6 h-6"}
    >
        <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 15.75l7.5-7.5 7.5 7.5" />
        <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 23.75l7.5-7.5 7.5 7.5" />
    </svg>
)

export const ChevronDownIcon = (props: Props) => (
    <svg
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        strokeWidth={1.5}
        stroke="currentColor"
        className={(props.className ? props.className : "") + " w-6 h-6"}
    >
        <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 8.25l-7.5 7.5-7.5-7.5" />
    </svg>
)
