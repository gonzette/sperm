/**
 * Definition for binary tree
 * struct TreeNode {
 *     int val;
 *     TreeNode *left;
 *     TreeNode *right;
 *     TreeNode(int x) : val(x), left(NULL), right(NULL) {}
 * };
 */

#include <vector>
using namespace std;

struct TreeNode {
  int val;
  TreeNode* left;
  TreeNode* right;
  TreeNode(int x) : val(x), left(NULL), right(NULL) {}
};

class Solution {
public:
  bool hasPathSum(TreeNode* root, int sum) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    if(root) {
      return R(root, sum);
    }
    return false;
  }

  bool R(TreeNode* root, int sum) {
    if(root->left == NULL && root->right == NULL) {
      return sum == root->val;
    } else {
      if(root->left && R(root->left, sum - root->val)) {
        return true;
      }
      if(root->right && R(root->right, sum - root->val)) {
        return true;
      }
      return false;
    }
  }
};

int main() {
  return 0;
}
