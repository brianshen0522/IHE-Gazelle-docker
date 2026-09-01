export const metadata = {
  title: "Gazelle registration",
  description: "Gazelle registration page for user activation",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return <>{children}</>;
}
