/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.component.EblanDialogContainer
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanUser
import com.eblan.launcher.domain.model.EblanUserType
import kotlinx.coroutines.launch

@Composable
fun SelectApplicationDialog(
    modifier: Modifier = Modifier,
    eblanApplicationInfos: Map<EblanUser, List<EblanApplicationInfo>>,
    onDismissRequest: () -> Unit,
    onClick: (EblanApplicationInfo) -> Unit,
) {
    val horizontalPagerState = rememberPagerState(
        pageCount = {
            eblanApplicationInfos.keys.size
        },
    )

    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = "Select Application",
                style = MaterialTheme.typography.titleLarge,
            )

            if (eblanApplicationInfos.keys.size > 1) {
                EblanApplicationInfoTabRow(
                    currentPage = horizontalPagerState.currentPage,
                    eblanApplicationInfos = eblanApplicationInfos,
                    onAnimateScrollToPage = horizontalPagerState::animateScrollToPage,
                )

                HorizontalPager(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .fillMaxWidth(),
                    state = horizontalPagerState,
                ) { index ->
                    EblanApplicationInfosPage(
                        index = index,
                        eblanApplicationInfos = eblanApplicationInfos,
                        onClick = onClick,
                    )
                }
            } else {
                EblanApplicationInfosPage(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .fillMaxWidth(),
                    index = 0,
                    eblanApplicationInfos = eblanApplicationInfos,
                    onClick = onClick,
                )
            }

            TextButton(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(
                        end = 10.dp,
                        bottom = 10.dp,
                    ),
                onClick = onDismissRequest,
            ) {
                Text(text = "Cancel")
            }
        }
    }
}

@Composable
private fun EblanApplicationInfosPage(
    modifier: Modifier = Modifier,
    index: Int,
    eblanApplicationInfos: Map<EblanUser, List<EblanApplicationInfo>>,
    onClick: (EblanApplicationInfo) -> Unit,
) {
    val eblanUser = eblanApplicationInfos.keys.toList().getOrElse(
        index = index,
        defaultValue = {
            EblanUser(
                serialNumber = 0L,
                eblanUserType = EblanUserType.Personal,
                isPrivateSpaceEntryPointHidden = false,
            )
        },
    )

    LazyColumn(modifier = modifier) {
        items(eblanApplicationInfos[eblanUser].orEmpty()) { eblanApplicationInfo ->
            ListItem(
                headlineContent = { Text(text = eblanApplicationInfo.label) },
                leadingContent = {
                    AsyncImage(
                        model = eblanApplicationInfo.icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                    )
                },
                modifier = Modifier
                    .clickable {
                        onClick(eblanApplicationInfo)
                    }
                    .fillMaxWidth()
                    .padding(10.dp),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EblanApplicationInfoTabRow(
    currentPage: Int,
    eblanApplicationInfos: Map<EblanUser, List<EblanApplicationInfo>>,
    onAnimateScrollToPage: suspend (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    SecondaryTabRow(selectedTabIndex = currentPage) {
        eblanApplicationInfos.keys.forEachIndexed { index, eblanUser ->
            Tab(
                selected = currentPage == index,
                onClick = {
                    scope.launch {
                        onAnimateScrollToPage(index)
                    }
                },
                text = {
                    Text(
                        text = eblanUser.eblanUserType.name,
                        maxLines = 1,
                    )
                },
            )
        }
    }
}
