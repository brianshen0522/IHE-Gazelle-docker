import { AccessControlList } from "@/shared/types/AccessControlListTypes";

export type DatahouseItem = {
  id: string;
  type: string;
  content: any;
  date: string;
  references: DatahouseItemReference[];
  accessControlList: AccessControlList;
}

export type DatahouseItemReference = {
  name: string;
  value: string;
  refType: string;
  type: string;
}