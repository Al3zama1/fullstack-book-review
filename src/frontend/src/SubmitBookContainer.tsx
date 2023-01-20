import React, {FormEvent, useState} from "react";
import {RootState} from "./types";
import {login} from "./actions";
import {Button, Container, Form, Header, Message} from "semantic-ui-react";
import {connect, ConnectedProps} from "react-redux";
import {keycloakLogin} from "./KeycloakService";

const mapState = (state: RootState) => ({
    isAuthenticated: state.authentication.isAuthenticated,
    token: state.authentication.details?.token
})

const mapDispatch = {
    login
}

const connector = connect(mapState, mapDispatch)
type PropsFromRedux = ConnectedProps<typeof connector>
type Props = PropsFromRedux
const SubmitBookContainer: React.FC<Props> = ({isAuthenticated, token}) => {

    const [isbn, setIsbn] = useState<string | number | boolean | (string | number | boolean)[] | undefined>("");
    const [success, setSuccess] = useState<boolean>(false);
    const [errorMessage, setErrorMessage] = useState<string>("");
    const handleSubmit = (evt: FormEvent) => {
        evt.preventDefault()

        setSuccess(false)
        setErrorMessage("")

        fetch(`/api/v1/books`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                isbn: isbn
            })
        }).then(result => {
            if (result.status === 202) {
                setSuccess(true)
            }
            if (result.status == 422) {
                setErrorMessage('Your request could not be precessed')
            }
        }).catch(error => {
            setErrorMessage(`Book could not be added at this time, please try again later: ${error}`)
        })

    }

    return (
        <Container>
            <Header as='h2' textAlign='center'>Add New Book To System</Header>
            {isAuthenticated ?
                <Form size='large' onSubmit={(e: FormEvent) => handleSubmit(e)} success={success} error={errorMessage !== ''}>
                    <Message
                        success
                        header='This was a success'
                        content={
                        <span>You successfully added a new book to the system.</span>
                        }
                        />
                    <Message
                        error
                        header='There was an error'
                        content={errorMessage}
                        />
                    <Form.Input
                        label= 'Book ISBN'
                        placeholder='Enter the 13 digit book ISBN'
                        value={isbn}
                        onChange={(e:any) => setIsbn(e.target.value)}
                        required
                        />
                    <Button
                        id='add-book'
                        secondary
                        type='submit'>
                        Add new book
                    </Button>
                </Form>
                :
                <Message
                    icon='lock'
                    header='Restricted area'
                    content={<span>To add a new book to the system, please <span
                    style={{color: "rgb(30, 112, 191)", cursor: "pointer"}} onClick={() => keycloakLogin()}>login</span> first.</span>}
                />
            }
        </Container>

    );
}

export default connector(SubmitBookContainer)