export const arrayDistinctBy = <T, >(getProp: (item: T) => {}, array: T[]): T[] => {
  const set = new Set();
  return array
    .filter(current => {
      const value = getProp(current);
      if (set.has(value)) {
        return false;
      }
      set.add(value);
      return true;
    });
};
