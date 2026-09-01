/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import ProfileDetails from "../ProfileDetails";
import { ValidationProfile } from "@validation-portal/types/ValidationProfile";

vi.mock("../ProfileHistoryTable", () => ({
  __esModule: true,
  default: ({ profileId }: any) => <div data-testid="profile-history-table">History for {profileId}</div>,
}));

vi.mock("@gazelle/gazelle-component-ui", () => {
  const SidePanelSection = ({ id, title, children }: any) => (
    <section data-testid={`section-${id}`}>
      <h2>{title}</h2>
      {children}
    </section>
  );

  return {
    InfoRow: ({ label, value }: any) => (
      <div data-testid={`info-row-${label}`}>
        <span className="label">{label}</span>
        <span className="value">{value}</span>
      </div>
    ),
    TagSection: ({ items, labelKey, keyPrefix }: any) => (
      <div data-testid={`tag-section-${keyPrefix}`}>
        <span className="label">{labelKey}</span>
        <div className="tags">
          {items?.map((item: string, index: number) => (
            <span key={`${keyPrefix}-${index}`} className="tag">
              {item}
            </span>
          ))}
        </div>
      </div>
    ),
    SidePanel: {
      Section: SidePanelSection,
    },
  };
});

describe("ProfileDetails", () => {
  const mockValidationService = "Certificate Validator";

  const mockProfile: ValidationProfile = {
    profileID: "profile-123",
    profileName: "TLS Server Profile",
    version: "1.0.0",
    domain: "Healthcare",
    standards: ["HL7", "FHIR", "IHE"],
    coveredItems: ["Bundle", "Patient", "Observation"],
    tags: ["production", "validation", "healthcare"],
  };

  it("returns null when profile is undefined", () => {
    const { container } = render(<ProfileDetails profile={undefined} validationService={mockValidationService} />);

    expect(container.firstChild).toBeNull();
  });

  it("renders profile details section with title", () => {
    render(<ProfileDetails profile={mockProfile} validationService={mockValidationService} />);

    expect(screen.getByTestId("section-profile-details")).toBeInTheDocument();
    expect(screen.getByText("Validation profile")).toBeInTheDocument();
  });

  it("renders profile history section with title", () => {
    render(<ProfileDetails profile={mockProfile} validationService={mockValidationService} />);

    expect(screen.getByTestId("section-profile-history")).toBeInTheDocument();
    expect(screen.getByText("History")).toBeInTheDocument();
  });

  it("renders ProfileHistoryTable with correct profileId", () => {
    render(<ProfileDetails profile={mockProfile} validationService={mockValidationService} />);

    const historyTable = screen.getByTestId("profile-history-table");
    expect(historyTable).toBeInTheDocument();
    expect(historyTable).toHaveTextContent(`History for ${mockProfile.profileID}`);
  });

  it("renders all required profile information", () => {
    render(<ProfileDetails profile={mockProfile} validationService={mockValidationService} />);

    // Profile ID (always rendered)
    const profileIdRow = screen.getByTestId("info-row-Profile ID");
    expect(profileIdRow).toBeInTheDocument();
    expect(profileIdRow).toHaveTextContent(mockProfile.profileID);

    // Validation Service (always rendered)
    const serviceRow = screen.getByTestId("info-row-Provided by");
    expect(serviceRow).toBeInTheDocument();
    expect(serviceRow).toHaveTextContent(mockValidationService);
  });

  it("renders optional profile fields when present", () => {
    render(<ProfileDetails profile={mockProfile} validationService={mockValidationService} />);

    // Profile Name
    const nameRow = screen.getByTestId("info-row-Profile Name");
    expect(nameRow).toBeInTheDocument();
    expect(nameRow).toHaveTextContent(mockProfile.profileName!);

    // Version
    const versionRow = screen.getByTestId("info-row-Profile Version");
    expect(versionRow).toBeInTheDocument();
    expect(versionRow).toHaveTextContent(mockProfile.version!);

    // Domain
    const domainRow = screen.getByTestId("info-row-Domain");
    expect(domainRow).toBeInTheDocument();
    expect(domainRow).toHaveTextContent(mockProfile.domain!);
  });

  it("does not render optional fields when they are missing", () => {
    const minimalProfile: ValidationProfile = {
      profileID: "minimal-profile",
    };

    render(<ProfileDetails profile={minimalProfile} validationService={mockValidationService} />);

    // Required fields should be present
    expect(screen.getByTestId("info-row-Profile ID")).toBeInTheDocument();
    expect(screen.getByTestId("info-row-Provided by")).toBeInTheDocument();

    // Optional fields should not be present
    expect(screen.queryByTestId("info-row-Profile Name")).not.toBeInTheDocument();
    expect(screen.queryByTestId("info-row-Profile Version")).not.toBeInTheDocument();
    expect(screen.queryByTestId("info-row-Domain")).not.toBeInTheDocument();
  });

  it("renders standards TagSection with correct data", () => {
    render(<ProfileDetails profile={mockProfile} validationService={mockValidationService} />);

    const standardsSection = screen.getByTestId("tag-section-standard");
    expect(standardsSection).toBeInTheDocument();
    expect(standardsSection).toHaveTextContent("StandardsHL7FHIRIHE");

    // Check all standards are rendered
    mockProfile.standards?.forEach((standard) => {
      expect(standardsSection).toHaveTextContent(standard);
    });
  });

  it("renders coveredItems TagSection with correct data", () => {
    render(<ProfileDetails profile={mockProfile} validationService={mockValidationService} />);

    const coveredItemsSection = screen.getByTestId("tag-section-covered");
    expect(coveredItemsSection).toBeInTheDocument();
    expect(coveredItemsSection).toHaveTextContent("Applies OnBundlePatientObservation");

    // Check all covered items are rendered
    mockProfile.coveredItems?.forEach((item) => {
      expect(coveredItemsSection).toHaveTextContent(item);
    });
  });

  it("renders tags TagSection with correct data", () => {
    render(<ProfileDetails profile={mockProfile} validationService={mockValidationService} />);

    const tagsSection = screen.getByTestId("tag-section-detail-tag");
    expect(tagsSection).toBeInTheDocument();
    expect(tagsSection).toHaveTextContent("Tagsproductionvalidationhealthcare");

    // Check all tags are rendered
    mockProfile.tags?.forEach((tag) => {
      expect(tagsSection).toHaveTextContent(tag);
    });
  });

  it("handles profile with empty arrays for standards, coveredItems, and tags", () => {
    const profileWithEmptyArrays: ValidationProfile = {
      profileID: "profile-empty",
      profileName: "Empty Profile",
      standards: [],
      coveredItems: [],
      tags: [],
    };

    render(<ProfileDetails profile={profileWithEmptyArrays} validationService={mockValidationService} />);

    // TagSections should still be rendered but with no tags
    expect(screen.getByTestId("tag-section-standard")).toBeInTheDocument();
    expect(screen.getByTestId("tag-section-covered")).toBeInTheDocument();
    expect(screen.getByTestId("tag-section-detail-tag")).toBeInTheDocument();
  });

  it("renders all sections in correct order", () => {
    render(<ProfileDetails profile={mockProfile} validationService={mockValidationService} />);

    const sections = screen.getAllByRole("heading", { level: 2 });
    expect(sections).toHaveLength(2);
    expect(sections[0]).toHaveTextContent("Validation profile");
    expect(sections[1]).toHaveTextContent("History");
  });

  it("passes validationService correctly to InfoRow", () => {
    const customService = "Custom Validation Service";
    render(<ProfileDetails profile={mockProfile} validationService={customService} />);

    const serviceRow = screen.getByTestId("info-row-Provided by");
    expect(serviceRow).toHaveTextContent(customService);
  });
});
