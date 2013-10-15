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
#include <cmath>
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
  bool isBalanced(TreeNode* root) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    bool bal;
    R(root, &bal);
    return bal;
  }

  int R(TreeNode* root, bool* balanced) {
    if(root == NULL) {
      *balanced = true;
      return 0;
    }
    bool lb, rb;
    int l = R(root->left, &lb);
    int r = R(root->right, &rb);
    if(!lb || !rb || abs(l - r) > 1) {
      *balanced = false;
    } else {
      *balanced = true;
    }
    return 1 + max(l, r);
  }
};

int main() {
  return 0;
}
