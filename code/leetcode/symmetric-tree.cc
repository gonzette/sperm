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
  bool isSymmetric(TreeNode* root) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    if(root == NULL) return true;
    return isMirror(root->left, root->right);
  }
  bool isMirror(TreeNode* x, TreeNode* y) {
    if(x == NULL && y == NULL) return true;
    if(x == NULL || y == NULL) return false;
    if(x->val != y->val) return false;
    return isMirror(x->left, y->right) && isMirror(x->right, y->left);
  }
};

int main() {
  return 0;
}
