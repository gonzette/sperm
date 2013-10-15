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
#include <algorithm>
using namespace std;


struct TreeNode {
  int val;
  TreeNode* left;
  TreeNode* right;
  TreeNode(int x) : val(x), left(NULL), right(NULL) {}
};

class Solution {
public:
  int minDepth(TreeNode* root) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    if(root == NULL) {
      return 0;
    }
    if(root->left == NULL && root->right == NULL) {
      return 1;
    } else if(root->left == NULL) {
      return 1 + minDepth(root->right);
    } else if(root->right == NULL) {
      return 1 + minDepth(root->left);
    } else {
      int l = minDepth(root->left);
      int r = minDepth(root->right);
      return 1 + min(l, r);
    }
  }
};

int main() {
  return 0;
}
