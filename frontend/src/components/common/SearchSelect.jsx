import { useEffect, useState } from 'react';
import useDebounce from '@/hooks/useDebounce';

export default function SearchSelect({
  placeholder,
  fetchOptions,
  getLabel = (item) => item.name,
  onSelect,
  disabled,
}) {
  const [query, setQuery] = useState('');
  const [items, setItems] = useState([]);
  const [open, setOpen] = useState(false);
  const debouncedQuery = useDebounce(query);

  useEffect(() => {
    if (!open || disabled) return;
    let ignore = false;
    fetchOptions(debouncedQuery).then((data) => {
      if (!ignore) setItems(data.items ?? []);
    });
    return () => {
      ignore = true;
    };
  }, [debouncedQuery, open, disabled, fetchOptions]);

  const select = (item) => {
    setQuery(getLabel(item));
    setOpen(false);
    onSelect(item);
  };

  return (
    <div className="search-select">
      <input
        value={query}
        placeholder={placeholder}
        disabled={disabled}
        onFocus={() => setOpen(true)}
        onChange={(e) => setQuery(e.target.value)}
      />
      {open && items.length > 0 && (
        <ul className="search-select__list">
          {items.map((item) => (
            <li key={item.id ?? item.code ?? item.jmcd} onMouseDown={() => select(item)}>
              {getLabel(item)}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
