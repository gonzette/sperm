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
#include <climits>
using namespace std;

struct TreeNode {
  int val;
  TreeNode* left;
  TreeNode* right;
  TreeNode(int x) : val(x), left(NULL), right(NULL) {}
};


class Solution {
public:
  bool isValidBST(TreeNode* root) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    if(root == NULL) return true;
    int xmin, xmax;
    return isValid(root, xmin, xmax);
  }

  bool isValid(TreeNode* root, int& xmin, int& xmax) {
    xmin = root->val;
    xmax = root->val;
    int t1, t2;
    if(root->left) {
      if(!isValid(root->left, t1, t2)) return false;
      if(t2 >= root->val) return false;
      xmin = min(xmin, t1);
      xmax = max(xmax, t2);
    }
    if(root->right) {
      if(!isValid(root->right, t1, t2)) return false;
      if(t1 <= root->val) return false;
      xmin = min(xmin, t1);
      xmax = max(xmax, t2);
    }
    return true;
  }
};

int main() {
  return 0;
}
