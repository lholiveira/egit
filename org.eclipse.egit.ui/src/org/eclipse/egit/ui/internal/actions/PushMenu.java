/*******************************************************************************
 * Copyright (C) 2014, Red Hat Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   - Mickael Istria (Red Hat Inc.) - 436669 Simply push workflow
 *******************************************************************************/
package org.eclipse.egit.ui.internal.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.egit.ui.Activator;
import org.eclipse.egit.ui.internal.UIText;
import org.eclipse.egit.ui.internal.repository.tree.BranchesNode;
import org.eclipse.egit.ui.internal.repository.tree.LocalNode;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryNode;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * This is the definition of the Push menu on a given node. Depending on the
 * node, it will show either "Push Branch '...'" or "Push HEAD".
 */
public class PushMenu extends CompoundContributionItem implements
		IWorkbenchContribution {

	private IServiceLocator serviceLocator;

	private ISelectionService selectionService;

	/**	 */
	public PushMenu() {
		this(null);
	}

	/**
	 * @param id
	 */
	public PushMenu(String id) {
		super(id);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	public void initialize(IServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
		this.selectionService = (ISelectionService) serviceLocator
				.getService(ISelectionService.class);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		List<IContributionItem> res = new ArrayList<IContributionItem>();

		if (this.selectionService != null
				&& this.selectionService.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) this.selectionService
					.getSelection();
			Object selected = sel.getFirstElement();
			if (selected instanceof IAdaptable) {
				Object adapter = ((IAdaptable) selected)
						.getAdapter(IProject.class);
				if (adapter != null)
					selected = adapter;
			}

			Repository repository = null;
			if (selected instanceof RepositoryNode)
				repository = ((RepositoryNode) selected).getRepository();
			else if (selected instanceof BranchesNode)
				repository = ((BranchesNode) selected).getRepository();
			else if (selected instanceof LocalNode)
				repository = ((LocalNode) selected).getRepository();
			else if ((selected instanceof IProject)) {
				RepositoryMapping mapping = RepositoryMapping
						.getMapping((IProject) selected);
				if (mapping != null)
					repository = mapping.getRepository();
			}

			if (repository != null) {
				try {
					String ref = repository.getFullBranch();
					String menuLabel = UIText.PushMenu_PushHEAD;
					if (ref.startsWith(Constants.R_HEADS)) {
						menuLabel = NLS.bind(UIText.PushMenu_PushBranch,
								Repository.shortenRefName(ref));
					}
					CommandContributionItemParameter params = new CommandContributionItemParameter(
							this.serviceLocator, getClass().getName(),
							ActionCommands.PUSH_BRANCH_ACTION,
							CommandContributionItem.STYLE_PUSH);
					params.label = menuLabel;
					CommandContributionItem item = new CommandContributionItem(
							params);
					res.add(item);
				} catch (IOException ex) {
					Activator.handleError(ex.getLocalizedMessage(), ex, false);
				}
			}
		}
		return res.toArray(new IContributionItem[res.size()]);
	}
}
