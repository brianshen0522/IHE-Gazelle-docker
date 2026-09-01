/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import { describe, it, expect, vi } from "vitest";
import ConfirmModal from "../ConfirmModal";

// Mock i18n
vi.mock("react-i18next", () => ({ useTranslation: () => ({ t: (k: string) => k }) }));
// Mock @gazelle/gazelle-component-ui (Modal & Button)
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Modal: ({ isOpen, title, children }: any) =>
    isOpen ? (
      <div data-testid="modal-root">
        <h2>{title}</h2>
        {children}
      </div>
    ) : null,
  Button: ({ children, onClick, title }: any) => (
    <button onClick={onClick} title={title}>
      {children}
    </button>
  ),
}));

describe("ConfirmModal", () => {
  const baseProps = {
    title: "A title",
    isOpen: true,
    onCancel: vi.fn(),
    onContinue: vi.fn(),
    toggleModal: vi.fn(),
    textOnCancel: "cancel.text",
    textOnContinue: "continue.text",
  };

  it("rend le modal avec le titre et le contenu quand ouvert", () => {
    render(
      <ConfirmModal {...baseProps}>
        <p>Body</p>
      </ConfirmModal>,
    );
    expect(screen.getByTestId("modal-root")).toBeInTheDocument();
    expect(screen.getByText("A title")).toBeInTheDocument();
    expect(screen.getByText("Body")).toBeInTheDocument();
  });

  it("ne rend rien quand isOpen=false", () => {
    render(
      <ConfirmModal {...baseProps} isOpen={false}>
        <p>Hidden</p>
      </ConfirmModal>,
    );
    expect(screen.queryByTestId("modal-root")).toBeNull();
  });

  it("clique sur le bouton cancel (textOnCancel) déclenche onCancel", () => {
    const onContinue = vi.fn();
    const onCancel = vi.fn();
    render(
      <ConfirmModal {...baseProps} onContinue={onContinue} onCancel={onCancel}>
        <p>test</p>
      </ConfirmModal>,
    );
    fireEvent.click(screen.getByRole("button", { name: "cancel.text" }));
    expect(onCancel).toHaveBeenCalledTimes(1);
    expect(onContinue).not.toHaveBeenCalled();
  });

  it("clique sur le bouton continue (textOnContinue) déclenche onContinue", () => {
    const onContinue = vi.fn();
    const onCancel = vi.fn();
    render(
      <ConfirmModal {...baseProps} onContinue={onContinue} onCancel={onCancel}>
        <p>test</p>
      </ConfirmModal>,
    );
    fireEvent.click(screen.getByRole("button", { name: "continue.text" }));
    expect(onContinue).toHaveBeenCalledTimes(1);
    expect(onCancel).not.toHaveBeenCalled();
  });
});
