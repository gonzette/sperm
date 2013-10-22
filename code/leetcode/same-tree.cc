/**
 * Definition for binary tree
 * struct TreeNode {
 *     int val;
 *     TreeNode *left;
 *     TreeNode *right;
 *     TreeNode(int x) : val(x), left(NULL), right(NULL) {}
 * };
 */

#include <cstdio>

struct TreeNode {
  int val;
  TreeNode* left;
  TreeNode* right;
  TreeNode(int x) : val(x), left(NULL), right(NULL) {}
};

class Solution {
public:
  bool isSameTree(TreeNode* x, TreeNode* y) {
    if(x == NULL && y == NULL) return true;
    if(x == NULL || y == NULL) return false;
    if(x->val != y->val) return false;
    return isSameTree(x->left, y->left) && isSameTree(x->right, y->right);
  }
};

int main() {
  return 0;
}
