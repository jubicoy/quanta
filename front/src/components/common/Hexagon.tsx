import React from 'react';

interface Props {
  width: string;
  height: string;
  strokeColor: string;
  strokeWidth: number;
  fillColor: string;
}

export const Hexagon = (props: Props): React.ReactElement => {
  const { strokeColor, fillColor, strokeWidth, width, height } = props;
  const svgBox = `${0 - strokeWidth / 2} ${0 - strokeWidth / 2} ${512 + strokeWidth / 1} ${512 + strokeWidth / 1}`;
  return (
    <svg width={width} height={height} id='Layer_1' viewBox={svgBox} xmlns='http://www.w3.org/2000/svg'>
      <g><g>
        <path
          stroke={strokeColor}
          strokeWidth={strokeWidth}
          fill={fillColor}
          d='m485.291 129.408-224-128c-3.285-1.877-7.296-1.877-10.581 0l-224 128c-3.328 1.899-5.376 5.44-5.376 9.259v234.667c0 3.819 2.048 7.36 5.376 9.259l224 128c1.643.939 3.456 1.408 5.291 1.408s3.648-.469 5.291-1.408l224-128c3.328-1.899 5.376-5.44 5.376-9.259v-234.667c-.001-3.819-2.049-7.36-5.377-9.259z' /></g></g><g /><g /><g /><g /><g /><g /><g /><g /><g /><g /><g /><g /><g /><g /><g />
    </svg>
  );
};
