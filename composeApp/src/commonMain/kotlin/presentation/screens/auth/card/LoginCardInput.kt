package io.github.vrcmteam.vrcm.presentation.screens.auth.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.vrcmteam.vrcm.presentation.compoments.LoadingButton
import io.github.vrcmteam.vrcm.presentation.configs.locale.strings
import io.github.vrcmteam.vrcm.presentation.screens.auth.data.AuthUIState
import io.github.vrcmteam.vrcm.presentation.supports.PasswordMissEndVisualTransformation

@Composable
fun LoginCardInput(
    uiState: AuthUIState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var isFocus by remember { mutableStateOf(false) }
    Spacer(modifier = Modifier.height(36.dp))
    LoginTextField(
        imageVector = Icons.Outlined.AccountCircle,
        hintText = strings.authLoginUsername,
        textValue = uiState.username,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        onValueChange = onUsernameChange
    )
    Spacer(modifier = Modifier.height(16.dp))
    LoginTextField(
        imageVector = Icons.Outlined.Lock,
        hintText =  strings.authLoginPassword,
        textValue = uiState.password,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        visualTransformation =
        if (isFocus) {
            PasswordMissEndVisualTransformation()
        } else {
            PasswordVisualTransformation()
        },
        onFocusChanged = { focusState -> isFocus = focusState.isFocused },
        onValueChange = onPasswordChange
    )
    Spacer(modifier = Modifier.height(36.dp))
    LoadingButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 64.dp)
            .height(48.dp),
        text = strings.authLoginButton,
        enabled = uiState.password.isNotBlank() && uiState.username.isNotBlank(),
        isLoading = uiState.btnIsLoading,
        onClick = onClick
    )
}

@Composable
fun LoginTextField(
    imageVector: ImageVector,
    hintText: String,
    textValue: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onFocusChanged: (FocusState) -> Unit = {},
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.large
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .padding(start = 12.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            imageVector = imageVector,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.primary
        )
        BasicTextField(
            modifier = Modifier
                .onFocusChanged(onFocusChanged)
                .fillMaxWidth()
                .padding(end = 12.dp, top = 12.dp, bottom = 12.dp),
            value = textValue,
            decorationBox = { innerTextField ->
                Box {
                    if (textValue.isEmpty()) {
                        Text(
                            text = hintText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    innerTextField()
                }
            },
            onValueChange = { onValueChange(it.trim()) },
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
        )
    }
}
