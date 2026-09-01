import { StaticApp } from "@gazelle/gazelle-component-ui";
import { FileSearch } from "lucide-react";

const manageUsersRoles = ["role:gazelle_admin", "role:project_admin", "role:testing_session_manager", "org-adm:"];
const adminRoles = ["role:gazelle_admin", "role:project_admin", "role:testing_session_manager"];

export const USER_INTERFACE_STATIC_APPS: StaticApp[] = [
  {
    name: "Home",
    url: "/",
    icon: "Home",
    alwaysShow: true,
  },
  {
    name: "Test execution",
    serviceName: "Test Execution",
    // url has been mocked with adhoc test session for now
    url: "/gazelle/test-execution/test-suite",
    icon: "CirclePlay",
  },
  {
    name: "Simulation portal",
    serviceName: "Simulation Gateway",
    url: "/gazelle/simulation-portal/sequences",
    icon: "ServerCog",
  },
  {
    name: "Validation portal",
    serviceName: "Validation Gateway",
    url: "/gazelle/validation-portal/profiles",
    icon: "MonitorCheck",
  },
  {
    name: "Message capture",
    serviceName: "Proxy",
    url: "/gazelle/message-capture/messages",
    icon: "MailSearch",
  },
  {
    name: "Reports",
    serviceName: "Datahouse",
    url: "/gazelle/reports",
    icon: FileSearch as unknown as string,
    alwaysShow: true,
  },
  {
    name: "User management",
    serviceName: "User management",
    url: "/gazelle/user-management/users",
    icon: "UserCog",
    roles: manageUsersRoles,
  },
  {
    name: "Test bed configuration",
    url: "/gazelle/admin/home",
    icon: "Wrench",
    alwaysShow: true,
    roles: adminRoles,
  },
];
