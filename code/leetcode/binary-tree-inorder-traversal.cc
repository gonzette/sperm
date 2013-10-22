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
  vector<int> inorderTraversal(TreeNode* root) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    vector<int> vs;
    R(root, vs);
    return vs;
  }

  void R(TreeNode* root, vector<int>& vs) {
    if(root == NULL) return ;
    R(root->left, vs);
    vs.push_back(root->val);
    R(root->right, vs);
  }
};

int main() {
  return 0;
}
