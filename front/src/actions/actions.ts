export interface Action { type: 'text-set'; text: string }

export const setText = (text: string) => ({
  type: 'text-set',
  text
});
