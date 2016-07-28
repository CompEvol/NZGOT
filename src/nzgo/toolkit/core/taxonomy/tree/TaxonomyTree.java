package nzgo.toolkit.core.taxonomy.tree;

import nzgo.toolkit.core.taxonomy.TaxonSet;

/**
 * Taxonomy Tree
 * @author Walter Xie
 */
public class TaxonomyTree {

    protected TaxonomyNode root;

    public TaxonomyNode getRoot() {
        return root;
    }

    public TaxonomyNode getTaxonomyNode(String taxId) {
        return root.getTaxonomyNode(taxId);
    }

    public int getNodeCount() {
        return getAllNodes().size();
    }

    public TaxonSet<TaxonomyNode> getAllNodes() {
        return root.getAllNodes();
    }
}
