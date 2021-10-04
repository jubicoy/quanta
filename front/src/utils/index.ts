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

export class DeepSet extends Set {
  add (o: any) {
    for (const i of this) {
      if (this.deepCompare(o, i)) {
        return this;
      }
    }
    super.add.call(this, o);
    return this;
  };

  private deepCompare (o: any, i: any) {
    return JSON.stringify(o) === JSON.stringify(i);
  }
};
