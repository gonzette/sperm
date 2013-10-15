#include <cstdio>

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
  void flatten(TreeNode* root) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    if(root == NULL) {
      return ;
    }
    TreeNode* p = NULL;
    R(root, &p);
  }

  TreeNode* R(TreeNode* root, TreeNode** last) {
    if(root->left == NULL && root->right == NULL) {
      *last = root;
    } else if(root->left == NULL) {
      root->right = R(root->right, last);
    } else if(root->right == NULL) {
      root->right = R(root->left, last);
      root->left = NULL;
    } else {
      TreeNode* p = NULL;
      TreeNode* r = root->right;
      root->right = R(root->left, &p);
      root->left = NULL;
      p->right = R(r, last);
    }
    return root;
  }
};

int main() {
  TreeNode* x1 = new TreeNode(1);
  TreeNode* x2 = new TreeNode(2);
  //x1->left = x2;
  x1->right = x2;
  Solution s;
  s.flatten(x1);
  return 0;
}
