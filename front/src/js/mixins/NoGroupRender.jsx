var NoGroupRender = {
    renderNoGroup: function(groupName, key) {
        return (
            <div key={key} className="alert alert-warning">
                This Group name (<strong>{groupName}</strong>) does not exist.<br />
                Please confirm your URL.
            </div>
        );
    }
};

module.exports = NoGroupRender;
