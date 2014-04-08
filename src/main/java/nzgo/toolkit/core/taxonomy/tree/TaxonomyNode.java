package nzgo.toolkit.core.taxonomy.tree;

import nzgo.toolkit.core.taxonomy.Taxon;
import nzgo.toolkit.core.taxonomy.TaxonSet;

/**
 * Taxonomy Node
 * @author Walter Xie
 */
public class TaxonomyNode extends Taxon {
    // leaf node size = 0
    public TaxonSet<TaxonomyNode> childrenNodes = new TaxonSet<>();

    public TaxonomyNode getTaxonomyNode(String taxId) {
        for (TaxonomyNode node : getAllNodes()) {
            if (node.isSameAs(taxId))
                return node;
        }
        return null;
    }

    public int getNodeCount() {
        return getAllNodes().size();
    }

    // include itself, and it is in the last
    public TaxonSet<TaxonomyNode> getAllNodes() {
        TaxonSet<TaxonomyNode> allChildrenNodes = getAllChildrenNodes();
        allChildrenNodes.add(this);
        return allChildrenNodes;
    }

    public TaxonSet<TaxonomyNode> getAllChildrenNodes() {
        TaxonSet<TaxonomyNode> allNodes = new TaxonSet<>();
        for (TaxonomyNode child : childrenNodes) {
            TaxonSet<TaxonomyNode> allChildrenNodes = child.getAllChildrenNodes();
            if (allChildrenNodes.size() > 0) {
                allNodes.addAll(allChildrenNodes);
            }
        }
        return allNodes;
    }
}
