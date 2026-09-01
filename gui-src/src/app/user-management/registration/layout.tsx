import { IsValidStepContextProvider } from "@/app/user-management/context/IsValidStepContext";

export const metadata = {
  title: "Gazelle User Management",
  description: "Gazelle User Management registration page",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <div className="flex flex-col items-center">
      <IsValidStepContextProvider>{children}</IsValidStepContextProvider>
    </div>
  );
}
