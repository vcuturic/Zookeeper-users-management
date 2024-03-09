import { ZNode } from "./znode";

export interface MessageStructure {
    operation: string;
    zNode: ZNode;
}