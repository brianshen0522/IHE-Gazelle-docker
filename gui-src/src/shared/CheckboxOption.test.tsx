import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import CheckboxOption from "./CheckboxOption";

describe("CheckboxOption", () => {
  it("rend le label associé à l’input avec le texte enfant", () => {
    const handle = vi.fn();
    render(
      <CheckboxOption id="chk" type="checkbox" checked={false} onChange={handle} htmlFor="chk">
        Mon libellé
      </CheckboxOption>
    );
    const input = screen.getByRole("checkbox");
    const label = screen.getByText("Mon libellé");
    expect(label.tagName).toBe("LABEL");
    expect(label).toBeInTheDocument();
    expect(input).toHaveAttribute("id", "chk");
  });

  it("déclenche onChange lors d’un clic", () => {
    const handle = vi.fn();
    render(
      <CheckboxOption id="c1" type="checkbox" checked={false} onChange={handle} htmlFor="c1">
        Lib
      </CheckboxOption>
    );
    fireEvent.click(screen.getByRole("checkbox"));
    expect(handle).toHaveBeenCalledTimes(1);
  });

  it("se comporte comme un composant contrôlé (valeur reflète la prop checked)", () => {
    // Mock controlled state
    let checked = false;
    const handleChange = vi.fn(() => {
      checked = !checked;
    });
    const { rerender } = render(
      <CheckboxOption id="c2" type="checkbox" checked={checked} onChange={handleChange} htmlFor="c2">
        Ctrl
      </CheckboxOption>
    );
    const input = screen.getByRole("checkbox");
    expect((input as HTMLInputElement).checked).toBe(false);
    fireEvent.click(input);
    expect(handleChange).toHaveBeenCalledTimes(1);
    // Simulate parent updating checked prop
    rerender(
      <CheckboxOption id="c2" type="checkbox" checked={true} onChange={handleChange} htmlFor="c2">
        Ctrl
      </CheckboxOption>
    );
    expect((screen.getByRole("checkbox") as HTMLInputElement).checked).toBe(true);
  });
});
