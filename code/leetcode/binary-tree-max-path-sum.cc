#include <cstdio>
#include <algorithm>
using namespace std;

/**
 * Definition for binary tree
 * struct TreeNode {
 *     int val;
 *     TreeNode *left;
 *     TreeNode *right;
 *     TreeNode(int x) : val(x), left(NULL), right(NULL) {}
 * };
 */

struct TreeNode {
  int val;
  TreeNode* left;
  TreeNode* right;
  TreeNode(int x) : val(x), left(NULL), right(NULL) {}
};

class Solution {
public:
  int maxPathSum(TreeNode* root) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    if(root == NULL) {
      return 0;
    }
    int p;
    int s = side(root, &p);
    return max(s, p);
  }

  // path means max sum in root, but not contains root node,
  // so it does not contribute the parent.(but if contains root node, it doesn't matter)
  // because we just get max of it.
  // function return value max sum including root node.
  int side(TreeNode* root, int* path) {
    if(root->left == NULL && root->right == NULL) {
      *path = root->val;
      return root->val;
    } else if(root->left == NULL) {
      int rp;
      int r = side(root->right, &rp);
      *path = max(r, rp);
      return max(0, r) + root->val;
    } else if(root->right == NULL) {
      int lp;
      int l = side(root->left, &lp);
      *path = max(l, lp);
      return max(0, l) + root->val;
    } else {
      int lp, rp;
      int l = side(root->left, &lp);
      int r = side(root->right, &rp);
      int p = max(max(l, r), max(lp, rp));
      p = max(p, max(0, l) + max(0, r) + root->val);
      *path = p;
      return max(max(0, l), max(0, r)) + root->val;
    }
  }
};

int main() {
  return 0;
}
