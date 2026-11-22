export default function ChipSelect({ options, value = [], onChange, max = 5 }) {
  const toggle = (code) => {
    if (value.includes(code)) {
      onChange(value.filter((v) => v !== code));
    } else if (value.length < max) {
      onChange([...value, code]);
    }
  };

  return (
    <div>
      {options.map((option) => (
        <button
          key={option.code}
          type="button"
          className={value.includes(option.code) ? 'chip chip--selected' : 'chip'}
          onClick={() => toggle(option.code)}
        >
          {option.name}
        </button>
      ))}
    </div>
  );
}
