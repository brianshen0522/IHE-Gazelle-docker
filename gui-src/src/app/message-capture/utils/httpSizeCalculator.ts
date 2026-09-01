
/*
* This function calculates the size of HTTP headers without counting separators.
* It sums the byte lengths of header names and values, ignoring any separators like colons or newlines.
* It returns the total size in bytes.
* */

const getHeaderSizeWithoutSeparators = (headers: any) => {
  if (!headers || typeof headers !== 'object') {
    return 0;
  }
  let size = 0;
  for (const [key, value] of Object.entries(headers)) {
    size += Buffer.byteLength(key, 'utf8');
    size += typeof value === 'string' ? Buffer.byteLength(value, 'utf8') : 0;
  }
  return size;
};

export default getHeaderSizeWithoutSeparators;