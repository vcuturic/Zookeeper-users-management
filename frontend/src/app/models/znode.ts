export interface ZNode {
    name: string;
    children?: ZNode[];
    online?: boolean;
    type: number;
    showMenu?: boolean
}