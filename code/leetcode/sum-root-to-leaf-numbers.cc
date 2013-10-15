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
  int sumNumbers(TreeNode* root) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    if(root == NULL) {
      return 0;
    }
    int sum = 0;
    R(0, root, sum);
    return sum;
  }

  void R(int v, TreeNode* node, int& sum) {
    v = v * 10 + node-> val;
    if(!node->left && !node->right) {
      sum += v;
    } else {
      if(node->left) {
        R(v, node->left, sum);
      }
      if(node->right) {
        R(v, node->right, sum);
      }
    }
  }
};

int main() {
  Solution sol;
  {
    TreeNode* s = new TreeNode(1);
    TreeNode* l = new TreeNode(2);
    TreeNode* r = new TreeNode(3);
    s->left = l;
    s->right = r;
    printf("%d\n", sol.sumNumbers(s));
  }
  {
    TreeNode* s = new TreeNode(1);
    TreeNode* l = new TreeNode(2);
    s->left = l;
    printf("%d\n", sol.sumNumbers(s));
  }
  return 0;
}
