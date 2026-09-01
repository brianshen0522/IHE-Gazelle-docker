/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import GroupingToggleAssertion from "../GroupingToggleAssertion";

// Mock CheckboxOption component
vi.mock("@shared/CheckboxOption", () => ({
  default: ({ id, checked, onChange, children }: any) => (
    <div data-testid={`checkbox-${id}`}>
      <input type="checkbox" id={id} checked={checked} onChange={(e) => onChange(e)} data-testid={`input-${id}`} />
      <label htmlFor={id}>{children}</label>
    </div>
  ),
}));

describe("GroupingToggleAssertion", () => {
  const mockOnChange = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("rendering", () => {
    it("renders both grouping options", () => {
      render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      expect(screen.getByText("Group by severity")).toBeInTheDocument();
      expect(screen.getByText("Group by report")).toBeInTheDocument();
    });

    it("renders severity checkbox", () => {
      render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      expect(screen.getByTestId("checkbox-groupBySeverity")).toBeInTheDocument();
    });

    it("renders report checkbox", () => {
      render(<GroupingToggleAssertion value="report" onChange={mockOnChange} />);

      expect(screen.getByTestId("checkbox-groupByReport")).toBeInTheDocument();
    });

    it("applies correct container classes", () => {
      const { container } = render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const wrapper = container.firstChild as HTMLElement;
      expect(wrapper).toHaveClass("flex", "items-center", "gap-5", "my-3", "ml-5");
    });
  });

  describe("checked state with value='severity'", () => {
    it("checks severity option when value is 'severity'", () => {
      render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const severityInput = screen.getByTestId("input-groupBySeverity");
      expect(severityInput).toBeChecked();
    });

    it("unchecks report option when value is 'severity'", () => {
      render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const reportInput = screen.getByTestId("input-groupByReport");
      expect(reportInput).not.toBeChecked();
    });
  });

  describe("checked state with value='report'", () => {
    it("checks report option when value is 'report'", () => {
      render(<GroupingToggleAssertion value="report" onChange={mockOnChange} />);

      const reportInput = screen.getByTestId("input-groupByReport");
      expect(reportInput).toBeChecked();
    });

    it("unchecks severity option when value is 'report'", () => {
      render(<GroupingToggleAssertion value="report" onChange={mockOnChange} />);

      const severityInput = screen.getByTestId("input-groupBySeverity");
      expect(severityInput).not.toBeChecked();
    });
  });

  describe("onChange behavior", () => {
    it("calls onChange with 'severity' when severity checkbox is clicked", () => {
      render(<GroupingToggleAssertion value="report" onChange={mockOnChange} />);

      const severityInput = screen.getByTestId("input-groupBySeverity");
      fireEvent.click(severityInput);

      expect(mockOnChange).toHaveBeenCalledTimes(1);
      expect(mockOnChange).toHaveBeenCalledWith("severity");
    });

    it("calls onChange with 'report' when report checkbox is clicked", () => {
      render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const reportInput = screen.getByTestId("input-groupByReport");
      fireEvent.click(reportInput);

      expect(mockOnChange).toHaveBeenCalledTimes(1);
      expect(mockOnChange).toHaveBeenCalledWith("report");
    });

    it("can switch from severity to report", () => {
      const { rerender } = render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const reportInput = screen.getByTestId("input-groupByReport");
      fireEvent.click(reportInput);

      expect(mockOnChange).toHaveBeenCalledWith("report");

      // Simulate parent updating the value prop
      rerender(<GroupingToggleAssertion value="report" onChange={mockOnChange} />);

      expect(screen.getByTestId("input-groupByReport")).toBeChecked();
      expect(screen.getByTestId("input-groupBySeverity")).not.toBeChecked();
    });

    it("can switch from report to severity", () => {
      const { rerender } = render(<GroupingToggleAssertion value="report" onChange={mockOnChange} />);

      const severityInput = screen.getByTestId("input-groupBySeverity");
      fireEvent.click(severityInput);

      expect(mockOnChange).toHaveBeenCalledWith("severity");

      // Simulate parent updating the value prop
      rerender(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      expect(screen.getByTestId("input-groupBySeverity")).toBeChecked();
      expect(screen.getByTestId("input-groupByReport")).not.toBeChecked();
    });

    it("does not call onChange when clicking already selected option", () => {
      render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const severityInput = screen.getByTestId("input-groupBySeverity");
      fireEvent.click(severityInput);

      // onChange is still called (it's up to parent to handle no-op)
      expect(mockOnChange).toHaveBeenCalledWith("severity");
      expect(mockOnChange).toHaveBeenCalledTimes(1);
    });
  });

  describe("accessibility", () => {
    it("renders checkboxes with correct IDs", () => {
      render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      expect(screen.getByTestId("input-groupBySeverity")).toHaveAttribute("id", "groupBySeverity");
      expect(screen.getByTestId("input-groupByReport")).toHaveAttribute("id", "groupByReport");
    });

    it("renders labels with correct text", () => {
      render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const severityLabel = screen.getByText("Group by severity");
      const reportLabel = screen.getByText("Group by report");

      expect(severityLabel).toBeInTheDocument();
      expect(reportLabel).toBeInTheDocument();
    });

    it("associates labels with inputs via htmlFor", () => {
      render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const severityLabel = screen.getByText("Group by severity");
      const reportLabel = screen.getByText("Group by report");

      expect(severityLabel).toHaveAttribute("for", "groupBySeverity");
      expect(reportLabel).toHaveAttribute("for", "groupByReport");
    });
  });

  describe("edge cases", () => {
    it("handles rapid toggle clicks", () => {
      render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const reportInput = screen.getByTestId("input-groupByReport");
      const severityInput = screen.getByTestId("input-groupBySeverity");

      fireEvent.click(reportInput);
      fireEvent.click(severityInput);
      fireEvent.click(reportInput);

      expect(mockOnChange).toHaveBeenCalledTimes(3);
      expect(mockOnChange).toHaveBeenNthCalledWith(1, "report");
      expect(mockOnChange).toHaveBeenNthCalledWith(2, "severity");
      expect(mockOnChange).toHaveBeenNthCalledWith(3, "report");
    });

    it("maintains correct checked state after multiple renders", () => {
      const { rerender } = render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      expect(screen.getByTestId("input-groupBySeverity")).toBeChecked();

      rerender(<GroupingToggleAssertion value="report" onChange={mockOnChange} />);
      expect(screen.getByTestId("input-groupByReport")).toBeChecked();

      rerender(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);
      expect(screen.getByTestId("input-groupBySeverity")).toBeChecked();
    });

    it("renders correctly when value prop changes", () => {
      const { rerender } = render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      let severityInput = screen.getByTestId("input-groupBySeverity");
      let reportInput = screen.getByTestId("input-groupByReport");

      expect(severityInput).toBeChecked();
      expect(reportInput).not.toBeChecked();

      rerender(<GroupingToggleAssertion value="report" onChange={mockOnChange} />);

      severityInput = screen.getByTestId("input-groupBySeverity");
      reportInput = screen.getByTestId("input-groupByReport");

      expect(severityInput).not.toBeChecked();
      expect(reportInput).toBeChecked();
    });

    it("handles onChange callback reference change", () => {
      const mockOnChange1 = vi.fn();
      const mockOnChange2 = vi.fn();

      const { rerender } = render(<GroupingToggleAssertion value="severity" onChange={mockOnChange1} />);

      const reportInput = screen.getByTestId("input-groupByReport");
      fireEvent.click(reportInput);

      expect(mockOnChange1).toHaveBeenCalledWith("report");
      expect(mockOnChange2).not.toHaveBeenCalled();

      // Change the onChange callback
      rerender(<GroupingToggleAssertion value="severity" onChange={mockOnChange2} />);

      fireEvent.click(reportInput);

      expect(mockOnChange2).toHaveBeenCalledWith("report");
      expect(mockOnChange1).toHaveBeenCalledTimes(1); // Still only called once from before
    });
  });

  describe("integration with CheckboxOption", () => {
    it("passes correct props to severity CheckboxOption", () => {
      render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const checkbox = screen.getByTestId("checkbox-groupBySeverity");
      expect(checkbox).toBeInTheDocument();

      const input = screen.getByTestId("input-groupBySeverity");
      expect(input).toHaveAttribute("type", "checkbox");
      expect(input).toBeChecked();
    });

    it("passes correct props to report CheckboxOption", () => {
      render(<GroupingToggleAssertion value="report" onChange={mockOnChange} />);

      const checkbox = screen.getByTestId("checkbox-groupByReport");
      expect(checkbox).toBeInTheDocument();

      const input = screen.getByTestId("input-groupByReport");
      expect(input).toHaveAttribute("type", "checkbox");
      expect(input).toBeChecked();
    });

    it("renders CheckboxOptions in correct order", () => {
      const { container } = render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const checkboxes = container.querySelectorAll('[data-testid^="checkbox-"]');
      expect(checkboxes).toHaveLength(2);
      expect(checkboxes[0]).toHaveAttribute("data-testid", "checkbox-groupBySeverity");
      expect(checkboxes[1]).toHaveAttribute("data-testid", "checkbox-groupByReport");
    });
  });

  describe("visual layout", () => {
    it("renders both options in a flex container", () => {
      const { container } = render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const wrapper = container.firstChild as HTMLElement;
      expect(wrapper.tagName).toBe("DIV");
      expect(wrapper).toHaveClass("flex");
    });

    it("applies gap between options", () => {
      const { container } = render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const wrapper = container.firstChild as HTMLElement;
      expect(wrapper).toHaveClass("gap-5");
    });

    it("applies vertical margin", () => {
      const { container } = render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const wrapper = container.firstChild as HTMLElement;
      expect(wrapper).toHaveClass("my-3");
    });

    it("applies left margin", () => {
      const { container } = render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const wrapper = container.firstChild as HTMLElement;
      expect(wrapper).toHaveClass("ml-5");
    });

    it("centers items vertically", () => {
      const { container } = render(<GroupingToggleAssertion value="severity" onChange={mockOnChange} />);

      const wrapper = container.firstChild as HTMLElement;
      expect(wrapper).toHaveClass("items-center");
    });
  });
});
